package br.com.contabilidade.presentation.controller;

import br.com.contabilidade.application.usecase.ConsultarLancamentoUseCase;
import br.com.contabilidade.application.usecase.CriarLancamentoUseCase;
import br.com.contabilidade.application.usecase.CriarLancamentoUseCase.PartidaCommand;
import br.com.contabilidade.application.usecase.EstornarLancamentoUseCase;
import br.com.contabilidade.infrastructure.security.UsuarioAutenticado;
import br.com.contabilidade.presentation.dto.LancamentoDtos.CriarLancamentoRequest;
import br.com.contabilidade.presentation.dto.LancamentoDtos.EstornoRequest;
import br.com.contabilidade.presentation.dto.LancamentoDtos.LancamentoCriadoResponse;
import br.com.contabilidade.presentation.dto.LancamentoDtos.LancamentoDetalheResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.V1 + "/lancamentos")
@Tag(name = "Lançamentos")
public class LancamentoController {
    private final CriarLancamentoUseCase criar;
    private final EstornarLancamentoUseCase estornar;
    private final ConsultarLancamentoUseCase consultar;

    public LancamentoController(CriarLancamentoUseCase criar, EstornarLancamentoUseCase estornar, ConsultarLancamentoUseCase consultar) {
        this.criar = criar; this.estornar = estornar; this.consultar = consultar;
    }

    @PostMapping
    public ResponseEntity<LancamentoCriadoResponse> criar(
            @Valid @RequestBody CriarLancamentoRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Chave de idempotência (RNF-08); tratada por filtro") @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        var partidas = req.partidas().stream().map(p -> new PartidaCommand(p.contaId(), p.tipo(), p.valor())).toList();
        var lancamento = criar.executar(new CriarLancamentoUseCase.Command(req.data(), req.historico(), req.origem(), partidas, usuario.id()));
        return ResponseEntity.created(URI.create(ApiPaths.V1 + "/lancamentos/" + lancamento.getId()))
                .body(LancamentoCriadoResponse.de(lancamento));
    }

    @GetMapping("/{id}")
    public LancamentoDetalheResponse detalhar(@PathVariable UUID id) {
        return LancamentoDetalheResponse.de(consultar.executar(id));
    }

    @PostMapping("/{id}/estorno")
    public ResponseEntity<LancamentoCriadoResponse> estornar(@PathVariable UUID id, @Valid @RequestBody EstornoRequest req,
                                                             @AuthenticationPrincipal UsuarioAutenticado usuario) {
        var estorno = estornar.executar(new EstornarLancamentoUseCase.Command(id, req.motivo(), usuario.id()));
        return ResponseEntity.created(URI.create(ApiPaths.V1 + "/lancamentos/" + estorno.getId()))
                .body(LancamentoCriadoResponse.de(estorno));
    }
}
