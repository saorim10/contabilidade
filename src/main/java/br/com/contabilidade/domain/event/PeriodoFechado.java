package br.com.contabilidade.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PeriodoFechado(UUID periodoId, String competencia, UUID usuarioId, Instant ocorridoEm) implements DomainEvent {
    @Override public String tipo() { return "PeriodoFechado"; }
    @Override public UUID agregadoId() { return periodoId; }
    @Override public Map<String, Object> payload() { return Map.of("competencia", competencia); }
}
