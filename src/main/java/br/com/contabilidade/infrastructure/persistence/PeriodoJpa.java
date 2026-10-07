package br.com.contabilidade.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "periodo_contabil")
public class PeriodoJpa {
    @Id UUID id;
    int ano;
    int mes;
    String status;
    Instant fechadoEm;
    UUID fechadoPor;
}
