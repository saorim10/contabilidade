package br.com.contabilidade.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoAuditoriaJpaRepository extends JpaRepository<EventoAuditoriaJpa, UUID> {
    Page<EventoAuditoriaJpa> findByAgregadoId(UUID agregadoId, Pageable pageable);
}
