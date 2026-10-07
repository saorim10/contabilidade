package br.com.contabilidade.infrastructure.security;

import java.time.Instant;
import java.util.Arrays;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {
    public record Token(String token, String tipo, Instant expiraEm) {}

    private final UsuarioJpaRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AutenticacaoService(UsuarioJpaRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios; this.encoder = encoder; this.jwt = jwt;
    }

    public Token login(String username, String senha) {
        var usuario = usuarios.findByUsername(username)
                .filter(UsuarioJpa::isAtivo)
                .filter(u -> encoder.matches(senha, u.getSenhaHash()))
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        var autenticado = new UsuarioAutenticado(usuario.getId(), usuario.getUsername(),
                Arrays.stream(usuario.getPapeis().split(",")).map(String::trim).toList());
        var expira = jwt.expiracaoDeAgora();
        return new Token(jwt.gerar(autenticado, expira), "Bearer", expira);
    }
}
