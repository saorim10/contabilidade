package br.com.contabilidade.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Usuários de desenvolvimento. Desative em produção com SEED_USUARIOS=false. */
@Component
@ConditionalOnProperty(name = "app.seed.usuarios", havingValue = "true")
public class SeedUsuarios implements CommandLineRunner {
    private final UsuarioJpaRepository usuarios;
    private final PasswordEncoder encoder;
    private final String senhaPadrao;

    public SeedUsuarios(UsuarioJpaRepository usuarios, PasswordEncoder encoder, @Value("${app.seed.senha-padrao}") String senhaPadrao) {
        this.usuarios = usuarios; this.encoder = encoder; this.senhaPadrao = senhaPadrao;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() > 0) return;
        var hash = encoder.encode(senhaPadrao);
        usuarios.save(UsuarioJpa.novo("admin", hash, "ADMIN"));
        usuarios.save(UsuarioJpa.novo("contador", hash, "CONTADOR"));
        usuarios.save(UsuarioJpa.novo("senior", hash, "CONTADOR_SENIOR"));
        usuarios.save(UsuarioJpa.novo("auditor", hash, "AUDITOR"));
    }
}
