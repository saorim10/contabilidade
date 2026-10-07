package br.com.contabilidade.infrastructure.persistence;

import br.com.contabilidade.domain.enums.OrigemLancamento;
import br.com.contabilidade.domain.enums.StatusLancamento;
import br.com.contabilidade.domain.enums.TipoPartida;
import br.com.contabilidade.domain.model.LancamentoContabil;
import br.com.contabilidade.domain.model.LinhaRazao;
import br.com.contabilidade.domain.model.MovimentoConta;
import br.com.contabilidade.domain.model.Partida;
import br.com.contabilidade.domain.repository.LancamentoRepository;
import br.com.contabilidade.domain.vo.Competencia;
import br.com.contabilidade.domain.vo.Dinheiro;
import br.com.contabilidade.domain.vo.NumeroLancamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class LancamentoRepositoryAdapter implements LancamentoRepository {
    private final LancamentoJpaRepository jpa;

    public LancamentoRepositoryAdapter(LancamentoJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public LancamentoContabil salvar(LancamentoContabil l) {
        var existente = jpa.findById(l.getId());
        if (existente.isPresent()) { // RN-13: só o status pode mudar
            existente.get().status = l.getStatus().name();
            return toDomain(existente.get());
        }
        var e = new LancamentoJpa();
        e.id = l.getId(); e.numero = l.getNumero().toString(); e.data = l.getData();
        e.competencia = l.getCompetencia().toString(); e.historico = l.getHistorico();
        e.status = l.getStatus().name(); e.origem = l.getOrigem().name();
        e.criadoPor = l.getCriadoPor(); e.criadoEm = l.getCriadoEm(); e.estornoDeId = l.getEstornoDeId();
        for (var p : l.getPartidas()) {
            var pe = new PartidaJpa();
            pe.id = p.id(); pe.lancamento = e; pe.contaId = p.contaId(); pe.tipo = p.tipo().name(); pe.valor = p.valor().valor();
            e.partidas.add(pe);
        }
        return toDomain(jpa.save(e));
    }

    @Override public Optional<LancamentoContabil> buscarPorId(UUID id) { return jpa.findById(id).map(this::toDomain); }

    @Override
    public List<MovimentoConta> movimentos(LocalDate inicio, LocalDate fim) {
        return jpa.movimentos(inicio, fim).stream()
                .map(r -> new MovimentoConta(UUID.fromString(r[0].toString()), new BigDecimal(r[1].toString()), new BigDecimal(r[2].toString())))
                .toList();
    }

    @Override
    public MovimentoConta movimentoDaConta(UUID contaId, LocalDate ate) {
        var linhas = jpa.movimentoDaConta(contaId, ate);
        if (linhas.isEmpty()) return MovimentoConta.zero(contaId);
        var r = linhas.get(0);
        return new MovimentoConta(contaId, new BigDecimal(r[0].toString()), new BigDecimal(r[1].toString()));
    }

    @Override
    public List<LinhaRazao> razao(UUID contaId, LocalDate inicio, LocalDate fim) {
        return jpa.razao(contaId, inicio, fim).stream()
                .map(r -> new LinhaRazao((LocalDate) r[0], (String) r[1], (String) r[2], TipoPartida.valueOf((String) r[3]), (BigDecimal) r[4]))
                .toList();
    }

    @Override
    public boolean existeRascunho(Competencia c) {
        return jpa.existsByCompetenciaAndStatus(c.toString(), StatusLancamento.RASCUNHO.name());
    }

    private LancamentoContabil toDomain(LancamentoJpa e) {
        var partidas = e.partidas.stream()
                .map(p -> new Partida(p.id, p.contaId, TipoPartida.valueOf(p.tipo), Dinheiro.brl(p.valor)))
                .toList();
        return LancamentoContabil.reconstituir(e.id, NumeroLancamento.parse(e.numero), e.data, e.historico,
                StatusLancamento.valueOf(e.status), OrigemLancamento.valueOf(e.origem), partidas, e.criadoPor, e.criadoEm, e.estornoDeId);
    }
}
