package br.com.contabilidade.presentation.controller;

import br.com.contabilidade.application.usecase.SugerirContaUseCase;
import br.com.contabilidade.presentation.dto.AuditoriaIaDtos.SugerirContaRequest;
import br.com.contabilidade.presentation.dto.AuditoriaIaDtos.SugestaoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.V1 + "/ia")
@Tag(name = "IA (não autoritativa)")
public class IaController {
    private final SugerirContaUseCase sugerir;

    public IaController(SugerirContaUseCase sugerir) { this.sugerir = sugerir; }

    @PostMapping("/sugerir-conta")
    public SugestaoResponse sugerirConta(@Valid @RequestBody SugerirContaRequest req) {
        return sugerir.executar(req.descricao())
                .map(s -> new SugestaoResponse(s, "IA_SUGESTAO", false,
                        "Sugestão não persistida. Para efetivar, crie o lançamento com origem=IA_SUGESTAO."))
                .orElseGet(() -> new SugestaoResponse(null, "IA_SUGESTAO", false, "Nenhuma conta compatível encontrada."));
    }
}
