package br.com.contabilidade.presentation.controller;

import br.com.contabilidade.application.usecase.FecharPeriodoUseCase;
import br.com.contabilidade.application.usecase.GerenciarPeriodoUseCase;
import br.com.contabilidade.domain.vo.Competencia;
import br.com.contabilidade.infrastructure.security.UsuarioAutenticado;
import br.com.contabilidade.presentation.dto.PeriodoDtos.PeriodoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** {competencia} no formato AAAA-MM (ex.: 2025-01). */
@RestController
@RequestMapping(ApiPaths.V1 + "/periodos")
@Tag(name = "Períodos Contábeis")
public class PeriodoController {
    private final FecharPeriodoUseCase fechar;
    private final GerenciarPeriodoUseCase gerenciar;

    public PeriodoController(FecharPeriodoUseCase fechar, GerenciarPeriodoUseCase gerenciar) {
        this.fechar = fechar; this.gerenciar = gerenciar;
    }

    @GetMapping
    public List<PeriodoResponse> listar() { return gerenciar.listar().stream().map(PeriodoResponse::de).toList(); }

    @PostMapping("/{competencia}/abrir")
    public PeriodoResponse abrir(@PathVariable String competencia) {
        return PeriodoResponse.de(gerenciar.abrir(Competencia.parse(competencia)));
    }

    @PostMapping("/{competencia}/fechar")
    public PeriodoResponse fechar(@PathVariable String competencia, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return PeriodoResponse.de(fechar.executar(Competencia.parse(competencia), usuario.id()));
    }

    @PostMapping("/{competencia}/bloquear")
    public PeriodoResponse bloquear(@PathVariable String competencia) {
        return PeriodoResponse.de(gerenciar.bloquear(Competencia.parse(competencia)));
    }

    @PostMapping("/{competencia}/desbloquear")
    public PeriodoResponse desbloquear(@PathVariable String competencia) {
        return PeriodoResponse.de(gerenciar.desbloquear(Competencia.parse(competencia)));
    }

    /** Somente ADMIN. */
    @PostMapping("/{competencia}/reabrir")
    public PeriodoResponse reabrir(@PathVariable String competencia) {
        return PeriodoResponse.de(gerenciar.reabrir(Competencia.parse(competencia)));
    }
}
