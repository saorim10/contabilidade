package br.com.contabilidade.presentation.dto;

import br.com.contabilidade.domain.enums.StatusPeriodo;
import br.com.contabilidade.domain.model.PeriodoContabil;
import java.time.Instant;
import java.util.UUID;

public final class PeriodoDtos {
    private PeriodoDtos() {}

    public record PeriodoResponse(UUID id, String competencia, StatusPeriodo status, Instant fechadoEm, UUID fechadoPor) {
        public static PeriodoResponse de(PeriodoContabil p) {
            return new PeriodoResponse(p.getId(), p.getCompetencia().toString(), p.getStatus(), p.getFechadoEm(), p.getFechadoPor());
        }
    }
}
