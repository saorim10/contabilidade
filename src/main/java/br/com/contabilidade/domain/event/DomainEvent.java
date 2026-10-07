package br.com.contabilidade.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface DomainEvent {
    String tipo();
    UUID agregadoId();
    UUID usuarioId();
    Instant ocorridoEm();
    Map<String, Object> payload();
}
