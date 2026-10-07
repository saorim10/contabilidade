package br.com.contabilidade.domain.model;

import br.com.contabilidade.domain.event.DomainEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Registro imutável da trilha de auditoria / outbox. */
public record EventoAuditoria(UUID id, String tipoEvento, UUID agregadoId, Map<String, Object> payload,
                              Instant ocorridoEm, UUID usuarioId) {
    public static EventoAuditoria de(DomainEvent e) {
        return new EventoAuditoria(UUID.randomUUID(), e.tipo(), e.agregadoId(), e.payload(), e.ocorridoEm(), e.usuarioId());
    }
}
