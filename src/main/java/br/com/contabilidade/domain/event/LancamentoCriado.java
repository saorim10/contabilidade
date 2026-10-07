package br.com.contabilidade.domain.event;

import br.com.contabilidade.domain.enums.OrigemLancamento;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record LancamentoCriado(UUID lancamentoId, String numero, OrigemLancamento origem, BigDecimal total,
                               UUID usuarioId, Instant ocorridoEm) implements DomainEvent {
    @Override public String tipo() { return "LancamentoCriado"; }
    @Override public UUID agregadoId() { return lancamentoId; }
    @Override public Map<String, Object> payload() {
        return Map.of("numero", numero, "origem", origem.name(), "total", total);
    }
}
