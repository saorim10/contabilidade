package br.com.contabilidade.infrastructure.security;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpa, UUID> {
    Optional<UsuarioJpa> findByUsername(String username);
}
