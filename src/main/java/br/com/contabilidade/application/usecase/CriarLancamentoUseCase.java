package br.com.contabilidade.application.usecase;

import br.com.contabilidade.domain.enums.OrigemLancamento;
import br.com.contabilidade.domain.enums.TipoPartida;
import br.com.contabilidade.domain.exception.RecursoNaoEncontradoException;
import br.com.contabilidade.domain.exception.RegraNegocioException;
import br.com.contabilidade.domain.model.Conta;
import br.com.contabilidade.domain.model.LancamentoContabil;
import br.com.contabilidade.domain.model.Partida;
import br.com.contabilidade.domain.repository.ContaRepository;
import br.com.contabilidade.domain.repository.DomainEventPublisher;
import br.com.contabilidade.domain.repository.GeradorNumeroLancamento;
import br.com.contabilidade.domain.repository.LancamentoRepository;
import br.com.contabilidade.domain.vo.Competencia;
import br.com.contabilidade.domain.vo.Dinheiro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC-01 — Registrar lançamento contábil. */
@Service
public class CriarLancamentoUseCase {

    public record PartidaCommand(UUID contaId, TipoPartida tipo, BigDecimal valor) {}

    public record Command(LocalDate data, String historico, OrigemLancamento origem,
                          List<PartidaCommand> partidas, UUID usuarioId) {}

    private final LancamentoRepository lancamentos;
    private final ContaRepository contas;
    private final ValidadorPeriodo validadorPeriodo;
    private final GeradorNumeroLancamento gerador;
    private final DomainEventPublisher publisher;

    public CriarLancamentoUseCase(LancamentoRepository lancamentos, ContaRepository contas, ValidadorPeriodo validadorPeriodo,
                                  GeradorNumeroLancamento gerador, DomainEventPublisher publisher) {
        this.lancamentos = lancamentos; this.contas = contas; this.validadorPeriodo = validadorPeriodo;
        this.gerador = gerador; this.publisher = publisher;
    }

    @Transactional
    public LancamentoContabil executar(Command cmd) {
        OrigemLancamento origem = cmd.origem() == null ? OrigemLancamento.MANUAL : cmd.origem();
        if (origem == OrigemLancamento.ESTORNO) {
            throw new RegraNegocioException("RN-07", "Lançamentos de estorno só podem ser gerados pela operação de estorno");
        }
        validadorPeriodo.exigirAberto(cmd.data());
        validarContas(cmd.partidas());

        var numero = gerador.proximo(Competencia.of(cmd.data()));
        var partidas = cmd.partidas().stream()
                .map(p -> Partida.nova(p.contaId(), p.tipo(), Dinheiro.brl(p.valor())))
                .toList();
        var lancamento = LancamentoContabil.criar(numero, cmd.data(), cmd.historico(), origem, partidas, cmd.usuarioId());
        lancamento.confirmar(); // valida RN-01 / RN-02

        var salvo = lancamentos.salvar(lancamento);
        publisher.publicar(lancamento.coletarEventos());
        return salvo;
    }

    /** RN-03, RN-04, RN-09. */
    private void validarContas(List<PartidaCommand> partidas) {
        Set<UUID> ids = partidas.stream().map(PartidaCommand::contaId).collect(Collectors.toSet());
        Map<UUID, Conta> encontradas = contas.buscarPorIds(ids).stream()
                .collect(Collectors.toMap(Conta::getId, Function.identity()));
        for (UUID id : ids) {
            Conta conta = encontradas.get(id);
            if (conta == null) throw new RecursoNaoEncontradoException("Conta", id);
            if (!conta.podeReceberLancamento()) {
                throw new RegraNegocioException("RN-04", "Conta " + conta.getCodigo() + " está inativa");
            }
            if (contas.possuiFilhas(id)) {
                throw new RegraNegocioException("RN-09", "Conta " + conta.getCodigo() + " é sintética e não recebe lançamentos");
            }
        }
    }
}
