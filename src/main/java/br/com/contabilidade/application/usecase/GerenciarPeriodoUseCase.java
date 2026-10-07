package br.com.contabilidade.application.usecase;

import br.com.contabilidade.domain.exception.RecursoNaoEncontradoException;
import br.com.contabilidade.domain.exception.RegraNegocioException;
import br.com.contabilidade.domain.model.PeriodoContabil;
import br.com.contabilidade.domain.repository.PeriodoRepository;
import br.com.contabilidade.domain.vo.Competencia;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-04 — abrir, bloquear, desbloquear e reabrir períodos (o fechamento está em FecharPeriodoUseCase). */
@Service
public class GerenciarPeriodoUseCase {
    private final PeriodoRepository periodos;

    public GerenciarPeriodoUseCase(PeriodoRepository periodos) { this.periodos = periodos; }

    @Transactional(readOnly = true)
    public List<PeriodoContabil> listar() { return periodos.listar(); }

    @Transactional
    public PeriodoContabil abrir(Competencia competencia) {
        if (periodos.buscarPorCompetencia(competencia).isPresent()) {
            throw new RegraNegocioException("PERIODO-02", "Período " + competencia + " já existe");
        }
        return periodos.salvar(PeriodoContabil.abrir(competencia));
    }

    @Transactional public PeriodoContabil bloquear(Competencia c) { return alterar(c, PeriodoContabil::bloquear); }

    @Transactional public PeriodoContabil desbloquear(Competencia c) { return alterar(c, PeriodoContabil::desbloquear); }

    @Transactional public PeriodoContabil reabrir(Competencia c) { return alterar(c, PeriodoContabil::reabrir); }

    private PeriodoContabil alterar(Competencia competencia, Consumer<PeriodoContabil> acao) {
        var periodo = periodos.buscarPorCompetencia(competencia)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Período", competencia));
        acao.accept(periodo);
        return periodos.salvar(periodo);
    }
}
