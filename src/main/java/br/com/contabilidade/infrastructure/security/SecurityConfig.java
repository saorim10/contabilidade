package br.com.contabilidade.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.contabilidade.infrastructure.idempotency.IdempotencyFilter;
import br.com.contabilidade.presentation.controller.ApiPaths;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** RF-12: autorização por papéis (ADMIN, CONTADOR_SENIOR, CONTADOR, AUDITOR). */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwt, JdbcTemplate jdbc, ObjectMapper mapper) throws Exception {
        var jwtFilter = new JwtAuthFilter(jwt);
        var idempotencyFilter = new IdempotencyFilter(jdbc, mapper);
        String v1 = ApiPaths.V1;
        String[] escrita = {"CONTADOR", "CONTADOR_SENIOR", "ADMIN"};
        String[] senior = {"CONTADOR_SENIOR", "ADMIN"};

        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers(v1 + "/auth/login", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, v1 + "/auditoria/**").hasAnyRole("AUDITOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, v1 + "/periodos/*/reabrir").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, v1 + "/periodos/**").hasAnyRole(senior)
                .requestMatchers(HttpMethod.POST, v1 + "/contas", v1 + "/lancamentos", v1 + "/lancamentos/*/estorno", v1 + "/ia/**").hasAnyRole(escrita)
                .requestMatchers(HttpMethod.PATCH, v1 + "/contas/*/inativar").hasAnyRole(escrita)
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                        ProblemWriter.escrever(res, mapper, HttpStatus.UNAUTHORIZED, "Não autenticado", "Token ausente, inválido ou expirado"))
                .accessDeniedHandler((req, res, ex) ->
                        ProblemWriter.escrever(res, mapper, HttpStatus.FORBIDDEN, "Acesso negado", "Seu perfil não permite esta operação")))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(idempotencyFilter, JwtAuthFilter.class);
        return http.build();
    }
}
