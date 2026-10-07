package br.com.contabilidade.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "evento_auditoria")
public class EventoAuditoriaJpa {
    @Id UUID id;
    String tipoEvento;
    UUID agregadoId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    Map<String, Object> payload;

    Instant ocorridoEm;
    UUID usuarioId;
}
