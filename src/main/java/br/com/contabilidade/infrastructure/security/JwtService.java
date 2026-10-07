package br.com.contabilidade.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** RNF-10: JWT stateless (HS256). */
@Service
public class JwtService {
    private final SecretKey chave;
    private final long expiracaoMinutos;

    public JwtService(@Value("${app.jwt.secret}") String segredo, @Value("${app.jwt.expiracao-minutos}") long expiracaoMinutos) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMinutos = expiracaoMinutos;
    }

    public Instant expiracaoDeAgora() { return Instant.now().plusSeconds(expiracaoMinutos * 60); }

    public String gerar(UsuarioAutenticado u, Instant expiraEm) {
        return Jwts.builder()
                .subject(u.id().toString())
                .claim("username", u.username())
                .claim("papeis", u.papeis())
                .issuedAt(new Date())
                .expiration(Date.from(expiraEm))
                .signWith(chave)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
            List<String> papeis = ((List<Object>) c.get("papeis", List.class)).stream().map(Object::toString).toList();
            return Optional.of(new UsuarioAutenticado(UUID.fromString(c.getSubject()), c.get("username", String.class), papeis));
        } catch (JwtException | IllegalArgumentException | ClassCastException e) {
            return Optional.empty();
        }
    }
}
