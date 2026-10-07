package br.com.contabilidade.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/** Escreve respostas RFC 7807 fora do DispatcherServlet (filtros e entry points). */
public final class ProblemWriter {
    private ProblemWriter() {}

    public static void escrever(HttpServletResponse res, ObjectMapper mapper, HttpStatus status, String titulo, String detalhe)
            throws IOException {
        var problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        res.setStatus(status.value());
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/problem+json");
        mapper.writeValue(res.getWriter(), problema);
    }
}
