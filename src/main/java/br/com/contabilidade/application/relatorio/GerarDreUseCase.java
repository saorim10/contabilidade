package br.com.contabilidade.application.relatorio;

import br.com.contabilidade.application.relatorio.Relatorios.Dre;
import br.com.contabilidade.domain.enums.TipoConta;
import br.com.contabilidade.domain.model.Conta;
import br.com.contabilidade.domain.repository.ContaRepository;
import br.com.contabilidade.domain.repository.LancamentoRepository;
import br.com.contabilidade.domain.vo.Competencia;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-07 — Demonstração do Resultado do Exercício no intervalo de competências. */
@Service
public class GerarDreUseCase {
    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;

    public GerarDreUseCase(ContaRepository contas, LancamentoRepository lancamentos) {
        this.contas = contas; this.lancamentos = lancamentos;
    }

    @Transactional(readOnly = true)
    public Dre executar(Competencia inicial, Competencia finalC) {
        if (inicial.compareTo(finalC) > 0) throw new IllegalArgumentException("Competência inicial deve ser <= final");
        var todas = contas.listar(null, null);
        Map<UUID, Conta> porId = todas.stream().collect(Collectors.toMap(Conta::getId, Function.identity()));
        var agregado = Hierarquia.agregar(lancamentos.movimentos(inicial.inicio(), finalC.fim()), porId);

        var receitas = Hierarquia.total(TipoConta.RECEITA, todas, agregado);
        var despesas = Hierarquia.total(TipoConta.DESPESA, todas, agregado);
        return new Dre(inicial.toString(), finalC.toString(),
                Hierarquia.linhas(TipoConta.RECEITA, todas, agregado), Hierarquia.linhas(TipoConta.DESPESA, todas, agregado),
                receitas, despesas, receitas.subtract(despesas));
    }
}
