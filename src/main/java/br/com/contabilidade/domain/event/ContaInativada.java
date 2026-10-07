package br.com.contabilidade.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ContaInativada(UUID contaId, String codigo, UUID usuarioId, Instant ocorridoEm) implements DomainEvent {
    @Override public String tipo() { return "ContaInativada"; }
    @Override public UUID agregadoId() { return contaId; }
    @Override public Map<String, Object> payload() { return Map.of("codigo", codigo); }
}
