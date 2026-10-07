package br.com.contabilidade.application.usecase;

import br.com.contabilidade.domain.exception.RegraNegocioException;
import br.com.contabilidade.domain.model.PeriodoContabil;
import br.com.contabilidade.domain.repository.PeriodoRepository;
import br.com.contabilidade.domain.vo.Competencia;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/** RN-05 / RN-06: a data do lançamento deve pertencer a um período aberto (períodos inexistentes são abertos sob demanda). */
@Component
public class ValidadorPeriodo {
    private final PeriodoRepository periodos;

    public ValidadorPeriodo(PeriodoRepository periodos) { this.periodos = periodos; }

    public PeriodoContabil exigirAberto(LocalDate data) {
        var competencia = Competencia.of(data);
        var periodo = periodos.buscarPorCompetencia(competencia)
                .orElseGet(() -> periodos.salvar(PeriodoContabil.abrir(competencia)));
        if (!periodo.aceitaLancamento()) {
            throw new RegraNegocioException("RN-05",
                    "Período %s está %s e não aceita lançamentos".formatted(competencia, periodo.getStatus()));
        }
        return periodo;
    }
}
