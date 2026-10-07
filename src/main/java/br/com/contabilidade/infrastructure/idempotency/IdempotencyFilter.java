package br.com.contabilidade.infrastructure.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.contabilidade.infrastructure.security.ProblemWriter;
import br.com.contabilidade.infrastructure.security.UsuarioAutenticado;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * RNF-08: se a requisição de escrita traz o header Idempotency-Key e já existe resposta 2xx para (chave, usuário),
 * a resposta é reaproveitada sem reexecutar a operação. Limitação: requisições concorrentes com a mesma chave
 * podem executar duas vezes (não há lock distribuído).
 */
public class IdempotencyFilter extends OncePerRequestFilter {
    private static final Set<String> METODOS_DE_ESCRITA = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public IdempotencyFilter(JdbcTemplate jdbc, ObjectMapper mapper) { this.jdbc = jdbc; this.mapper = mapper; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String chave = req.getHeader("Idempotency-Key");
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (chave == null || chave.isBlank() || !METODOS_DE_ESCRITA.contains(req.getMethod())
                || auth == null || !(auth.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            chain.doFilter(req, res);
            return;
        }
        if (chave.length() > 100) {
            ProblemWriter.escrever(res, mapper, HttpStatus.BAD_REQUEST, "Idempotency-Key inválida", "Máximo de 100 caracteres");
            return;
        }

        var salvos = jdbc.query(
                "SELECT metodo, caminho, status, corpo FROM idempotencia WHERE chave = ? AND usuario_id = ?",
                (rs, i) -> new Object[]{rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4)},
                chave, usuario.id());

        if (!salvos.isEmpty()) {
            var r = salvos.get(0);
            if (!r[0].equals(req.getMethod()) || !r[1].equals(req.getRequestURI())) {
                ProblemWriter.escrever(res, mapper, HttpStatus.UNPROCESSABLE_ENTITY, "Idempotency-Key reutilizada",
                        "A chave já foi usada em outra requisição");
                return;
            }
            res.setStatus((Integer) r[2]);
            res.setCharacterEncoding("UTF-8");
            res.setContentType("application/json");
            res.setHeader("Idempotency-Replayed", "true");
            if (r[3] != null) res.getWriter().write((String) r[3]);
            return;
        }

        var wrapper = new ContentCachingResponseWrapper(res);
        chain.doFilter(req, wrapper);
        int status = wrapper.getStatus();
        if (status >= 200 && status < 300) {
            String corpo = new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            jdbc.update("""
                    INSERT INTO idempotencia (chave, usuario_id, metodo, caminho, status, corpo, criado_em)
                    VALUES (?, ?, ?, ?, ?, ?, now()) ON CONFLICT DO NOTHING
                    """, chave, usuario.id(), req.getMethod(), req.getRequestURI(), status, corpo);
        }
        wrapper.copyBodyToResponse();
    }
}
