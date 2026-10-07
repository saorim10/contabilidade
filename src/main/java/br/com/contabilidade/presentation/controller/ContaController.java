package br.com.contabilidade.presentation.controller;

import br.com.contabilidade.application.usecase.GerenciarContaUseCase;
import br.com.contabilidade.application.usecase.GerenciarContaUseCase.CriarCommand;
import br.com.contabilidade.domain.enums.NaturezaSaldo;
import br.com.contabilidade.domain.enums.TipoConta;
import br.com.contabilidade.infrastructure.security.UsuarioAutenticado;
import br.com.contabilidade.presentation.dto.ContaDtos.ContaResponse;
import br.com.contabilidade.presentation.dto.ContaDtos.CriarContaRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.V1 + "/contas")
@Tag(name = "Plano de Contas")
public class ContaController {
    private final GerenciarContaUseCase contas;

    public ContaController(GerenciarContaUseCase contas) { this.contas = contas; }

    @GetMapping
    public List<ContaResponse> listar(@RequestParam(required = false) TipoConta tipo,
                                      @RequestParam(required = false) NaturezaSaldo natureza) {
        return contas.listar(tipo, natureza).stream().map(ContaResponse::de).toList();
    }

    @PostMapping
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody CriarContaRequest req) {
        var conta = contas.criar(new CriarCommand(req.codigo(), req.nome(), req.tipo(), req.natureza()));
        return ResponseEntity.created(URI.create(ApiPaths.V1 + "/contas/" + conta.getId())).body(ContaResponse.de(conta));
    }

    @GetMapping("/{id}")
    public ContaResponse detalhar(@PathVariable UUID id) { return ContaResponse.de(contas.buscar(id)); }

    @PatchMapping("/{id}/inativar")
    public ContaResponse inativar(@PathVariable UUID id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ContaResponse.de(contas.inativar(id, usuario.id()));
    }
}
