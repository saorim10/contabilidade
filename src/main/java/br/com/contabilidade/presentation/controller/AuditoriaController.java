package br.com.contabilidade.presentation.controller;

import br.com.contabilidade.application.usecase.ConsultarAuditoriaUseCase;
import br.com.contabilidade.presentation.dto.AuditoriaIaDtos.EventoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.V1 + "/auditoria")
@Tag(name = "Auditoria")
public class AuditoriaController {
    private final ConsultarAuditoriaUseCase auditoria;

    public AuditoriaController(ConsultarAuditoriaUseCase auditoria) { this.auditoria = auditoria; }

    @GetMapping("/eventos")
    public List<EventoResponse> eventos(@RequestParam(required = false) UUID agregadoId,
                                        @RequestParam(defaultValue = "0") int pagina,
                                        @RequestParam(defaultValue = "50") int tamanho) {
        return auditoria.executar(agregadoId, pagina, tamanho).stream().map(EventoResponse::de).toList();
    }
}
