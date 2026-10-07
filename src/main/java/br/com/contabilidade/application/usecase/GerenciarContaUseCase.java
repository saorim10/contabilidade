package br.com.contabilidade.application.usecase;

import br.com.contabilidade.domain.enums.NaturezaSaldo;
import br.com.contabilidade.domain.enums.TipoConta;
import br.com.contabilidade.domain.exception.RecursoNaoEncontradoException;
import br.com.contabilidade.domain.exception.RegraNegocioException;
import br.com.contabilidade.domain.model.Conta;
import br.com.contabilidade.domain.repository.ContaRepository;
import br.com.contabilidade.domain.repository.DomainEventPublisher;
import br.com.contabilidade.domain.repository.LancamentoRepository;
import br.com.contabilidade.domain.vo.CodigoConta;
import br.com.contabilidade.domain.vo.Dinheiro;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-01 — CRUD (criar/consultar/inativar) do plano de contas hierárquico. */
@Service
public class GerenciarContaUseCase {

    public record CriarCommand(String codigo, String nome, TipoConta tipo, NaturezaSaldo natureza) {}

    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;
    private final DomainEventPublisher publisher;

    public GerenciarContaUseCase(ContaRepository contas, LancamentoRepository lancamentos, DomainEventPublisher publisher) {
        this.contas = contas; this.lancamentos = lancamentos; this.publisher = publisher;
    }

    /** A conta pai é resolvida pelo código (1.1.01 -> 1.1) e deve existir, estar ativa, ter o mesmo tipo e não ter movimento (RN-09). */
    @Transactional
    public Conta criar(CriarCommand cmd) {
        var codigo = new CodigoConta(cmd.codigo());
        if (contas.buscarPorCodigo(codigo).isPresent()) {
            throw new RegraNegocioException("CONTA-01", "Já existe conta com o código " + codigo);
        }
        UUID paiId = null;
        if (codigo.isFilha()) {
            var codigoPai = codigo.pai().orElseThrow();
            var pai = contas.buscarPorCodigo(codigoPai)
                    .orElseThrow(() -> new RegraNegocioException("CONTA-02", "Conta pai " + codigoPai + " não existe"));
            if (!pai.isAtiva()) throw new RegraNegocioException("CONTA-02", "Conta pai " + codigoPai + " está inativa");
            if (pai.getTipo() != cmd.tipo()) {
                throw new RegraNegocioException("CONTA-02", "Tipo da conta deve ser o mesmo da conta pai (" + pai.getTipo() + ")");
            }
            var mov = lancamentos.movimentoDaConta(pai.getId(), LancamentoRepository.FIM);
            if (mov.debitos().signum() != 0 || mov.creditos().signum() != 0) {
                throw new RegraNegocioException("RN-09", "Conta pai " + codigoPai + " já possui lançamentos e não pode virar sintética");
            }
            paiId = pai.getId();
        }
        return contas.salvar(Conta.criar(codigo, cmd.nome(), cmd.tipo(), cmd.natureza(), paiId));
    }

    @Transactional(readOnly = true)
    public Conta buscar(UUID id) {
        return contas.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Conta", id));
    }

    @Transactional(readOnly = true)
    public List<Conta> listar(TipoConta tipo, NaturezaSaldo natureza) { return contas.listar(tipo, natureza); }

    /** RN-08. */
    @Transactional
    public Conta inativar(UUID id, UUID usuarioId) {
        var conta = buscar(id);
        var mov = lancamentos.movimentoDaConta(id, LancamentoRepository.FIM);
        conta.inativar(Dinheiro.brl(mov.saldo(conta.getNatureza())), usuarioId);
        var salva = contas.salvar(conta);
        publisher.publicar(conta.coletarEventos());
        return salva;
    }
}
