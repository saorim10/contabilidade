package br.com.contabilidade.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conta")
public class ContaJpa {
    @Id UUID id;
    String codigo;
    String nome;
    String tipo;
    String natureza;
    UUID contaPaiId;
    boolean ativa;
    Instant criadoEm;
    Instant atualizadoEm;
}
