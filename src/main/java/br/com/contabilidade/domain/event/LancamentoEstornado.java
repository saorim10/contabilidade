package br.com.contabilidade.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record LancamentoEstornado(UUID lancamentoId, String numero, UUID estornoId, String numeroEstorno,
                                  String motivo, UUID usuarioId, Instant ocorridoEm) implements DomainEvent {
    @Override public String tipo() { return "LancamentoEstornado"; }
    @Override public UUID agregadoId() { return lancamentoId; }
    @Override public Map<String, Object> payload() {
        return Map.of("numero", numero, "estornoId", estornoId.toString(), "numeroEstorno", numeroEstorno, "motivo", motivo);
    }
}
