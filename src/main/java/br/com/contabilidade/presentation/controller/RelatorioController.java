package br.com.contabilidade.presentation.controller;

import br.com.contabilidade.application.relatorio.ConsultarRazaoUseCase;
import br.com.contabilidade.application.relatorio.GerarBalancoUseCase;
import br.com.contabilidade.application.relatorio.GerarBalanceteUseCase;
import br.com.contabilidade.application.relatorio.GerarDreUseCase;
import br.com.contabilidade.application.relatorio.Relatorios.Balanco;
import br.com.contabilidade.application.relatorio.Relatorios.Balancete;
import br.com.contabilidade.application.relatorio.Relatorios.Dre;
import br.com.contabilidade.application.relatorio.Relatorios.Razao;
import br.com.contabilidade.domain.vo.Competencia;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Parâmetros de competência no formato AAAA-MM. */
@RestController
@RequestMapping(ApiPaths.V1 + "/relatorios")
@Tag(name = "Relatórios")
public class RelatorioController {
    private final GerarBalanceteUseCase balancete;
    private final GerarBalancoUseCase balanco;
    private final GerarDreUseCase dre;
    private final ConsultarRazaoUseCase razao;

    public RelatorioController(GerarBalanceteUseCase balancete, GerarBalancoUseCase balanco, GerarDreUseCase dre, ConsultarRazaoUseCase razao) {
        this.balancete = balancete; this.balanco = balanco; this.dre = dre; this.razao = razao;
    }

    @GetMapping("/balancete")
    public Balancete balancete(@RequestParam String competencia) { return balancete.executar(Competencia.parse(competencia)); }

    @GetMapping("/balanco")
    public Balanco balanco(@RequestParam String competencia) { return balanco.executar(Competencia.parse(competencia)); }

    @GetMapping("/dre")
    public Dre dre(@RequestParam String competenciaInicial, @RequestParam(required = false) String competenciaFinal) {
        var inicial = Competencia.parse(competenciaInicial);
        return dre.executar(inicial, competenciaFinal == null ? inicial : Competencia.parse(competenciaFinal));
    }

    @GetMapping("/razao/{contaId}")
    public Razao razao(@PathVariable UUID contaId, @RequestParam String competencia) {
        return razao.executar(contaId, Competencia.parse(competencia));
    }
}
