package br.com.contabilidade.infrastructure.security;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class UsuarioJpa {
    @Id UUID id;
    String username;
    String senhaHash;
    String papeis;
    boolean ativo;

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getSenhaHash() { return senhaHash; }
    public String getPapeis() { return papeis; }
    public boolean isAtivo() { return ativo; }

    public static UsuarioJpa novo(String username, String senhaHash, String papeis) {
        var u = new UsuarioJpa();
        u.id = UUID.randomUUID(); u.username = username; u.senhaHash = senhaHash; u.papeis = papeis; u.ativo = true;
        return u;
    }
}
