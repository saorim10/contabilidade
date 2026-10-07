package br.com.contabilidade.infrastructure.persistence;

import br.com.contabilidade.domain.enums.StatusPeriodo;
import br.com.contabilidade.domain.model.PeriodoContabil;
import br.com.contabilidade.domain.repository.PeriodoRepository;
import br.com.contabilidade.domain.vo.Competencia;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class PeriodoRepositoryAdapter implements PeriodoRepository {
    private final PeriodoJpaRepository jpa;

    public PeriodoRepositoryAdapter(PeriodoJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public PeriodoContabil salvar(PeriodoContabil p) {
        var e = new PeriodoJpa();
        e.id = p.getId(); e.ano = p.getCompetencia().ano(); e.mes = p.getCompetencia().mes();
        e.status = p.getStatus().name(); e.fechadoEm = p.getFechadoEm(); e.fechadoPor = p.getFechadoPor();
        return toDomain(jpa.save(e));
    }

    @Override
    public Optional<PeriodoContabil> buscarPorCompetencia(Competencia c) {
        return jpa.findByAnoAndMes(c.ano(), c.mes()).map(this::toDomain);
    }

    @Override
    public List<PeriodoContabil> listar() {
        return jpa.findAll(Sort.by(Sort.Direction.DESC, "ano", "mes")).stream().map(this::toDomain).toList();
    }

    private PeriodoContabil toDomain(PeriodoJpa e) {
        return PeriodoContabil.reconstituir(e.id, new Competencia(e.ano, e.mes), StatusPeriodo.valueOf(e.status), e.fechadoEm, e.fechadoPor);
    }
}
