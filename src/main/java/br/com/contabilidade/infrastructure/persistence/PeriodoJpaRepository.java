package br.com.contabilidade.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodoJpaRepository extends JpaRepository<PeriodoJpa, UUID> {
    Optional<PeriodoJpa> findByAnoAndMes(int ano, int mes);
}
