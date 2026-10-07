package br.com.contabilidade.domain.repository;

import br.com.contabilidade.domain.model.PeriodoContabil;
import br.com.contabilidade.domain.vo.Competencia;
import java.util.List;
import java.util.Optional;

public interface PeriodoRepository {
    PeriodoContabil salvar(PeriodoContabil periodo);
    Optional<PeriodoContabil> buscarPorCompetencia(Competencia competencia);
    List<PeriodoContabil> listar();
}
