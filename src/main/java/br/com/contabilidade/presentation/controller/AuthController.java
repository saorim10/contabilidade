package br.com.contabilidade.presentation.controller;

import br.com.contabilidade.infrastructure.security.AutenticacaoService;
import br.com.contabilidade.presentation.dto.AuthDtos.LoginRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.V1 + "/auth")
@Tag(name = "Autenticação")
public class AuthController {
    private final AutenticacaoService autenticacao;

    public AuthController(AutenticacaoService autenticacao) { this.autenticacao = autenticacao; }

    @PostMapping("/login")
    public AutenticacaoService.Token login(@Valid @RequestBody LoginRequest req) {
        return autenticacao.login(req.username(), req.senha());
    }
}
