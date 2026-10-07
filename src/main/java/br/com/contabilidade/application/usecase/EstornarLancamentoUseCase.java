package br.com.contabilidade.application.usecase;

import br.com.contabilidade.domain.event.DomainEvent;
import br.com.contabilidade.domain.exception.RecursoNaoEncontradoException;
import br.com.contabilidade.domain.model.LancamentoContabil;
import br.com.contabilidade.domain.repository.DomainEventPublisher;
import br.com.contabilidade.domain.repository.GeradorNumeroLancamento;
import br.com.contabilidade.domain.repository.LancamentoRepository;
import br.com.contabilidade.domain.vo.Competencia;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC-02 — Estornar lançamento (o estorno é datado de hoje; o período atual precisa estar aberto). */
@Service
public class EstornarLancamentoUseCase {

    public record Command(UUID lancamentoId, String motivo, UUID usuarioId) {}

    private final LancamentoRepository lancamentos;
    private final ValidadorPeriodo validadorPeriodo;
    private final GeradorNumeroLancamento gerador;
    private final DomainEventPublisher publisher;
    private final Clock clock;

    public EstornarLancamentoUseCase(LancamentoRepository lancamentos, ValidadorPeriodo validadorPeriodo,
                                     GeradorNumeroLancamento gerador, DomainEventPublisher publisher, Clock clock) {
        this.lancamentos = lancamentos; this.validadorPeriodo = validadorPeriodo;
        this.gerador = gerador; this.publisher = publisher; this.clock = clock;
    }

    @Transactional
    public LancamentoContabil executar(Command cmd) {
        var original = lancamentos.buscarPorId(cmd.lancamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lançamento", cmd.lancamentoId()));
        LocalDate hoje = LocalDate.now(clock);
        validadorPeriodo.exigirAberto(hoje);

        var numero = gerador.proximo(Competencia.of(hoje));
        var estorno = original.estornar(cmd.motivo(), numero, hoje, cmd.usuarioId());

        lancamentos.salvar(original); // atualiza apenas o status (RN-13)
        var salvo = lancamentos.salvar(estorno);

        var eventos = new ArrayList<DomainEvent>(original.coletarEventos());
        eventos.addAll(estorno.coletarEventos());
        publisher.publicar(eventos);
        return salvo;
    }
}
