package br.com.contabilidade.presentation.dto;

import br.com.contabilidade.domain.model.EventoAuditoria;
import br.com.contabilidade.domain.model.SugestaoConta;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class AuditoriaIaDtos {
    private AuditoriaIaDtos() {}

    public record EventoResponse(UUID id, String tipoEvento, UUID agregadoId, Map<String, Object> payload,
                                 Instant ocorridoEm, UUID usuarioId) {
        public static EventoResponse de(EventoAuditoria e) {
            return new EventoResponse(e.id(), e.tipoEvento(), e.agregadoId(), e.payload(), e.ocorridoEm(), e.usuarioId());
        }
    }

    public record SugerirContaRequest(@NotBlank String descricao) {}

    /** Sugestão nunca é persistida; para efetivar, o contador cria o lançamento com origem=IA_SUGESTAO (RN-11). */
    public record SugestaoResponse(SugestaoConta sugestao, String origem, boolean persistida, String mensagem) {}
}
