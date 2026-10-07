package br.com.contabilidade.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lancamento")
public class LancamentoJpa {
    @Id UUID id;
    String numero;
    LocalDate data;
    String competencia;
    String historico;
    String status;
    String origem;
    UUID criadoPor;
    Instant criadoEm;
    UUID estornoDeId;

    @OneToMany(mappedBy = "lancamento", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<PartidaJpa> partidas = new ArrayList<>();
}
