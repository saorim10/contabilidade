package br.com.contabilidade.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContaJpaRepository extends JpaRepository<ContaJpa, UUID> {
    Optional<ContaJpa> findByCodigo(String codigo);
    boolean existsByContaPaiId(UUID contaPaiId);
}
