package br.com.contabilidade.application.relatorio;

import br.com.contabilidade.application.relatorio.Relatorios.Balancete;
import br.com.contabilidade.application.relatorio.Relatorios.BalanceteLinha;
import br.com.contabilidade.application.relatorio.Relatorios.Saldo;
import br.com.contabilidade.domain.model.Conta;
import br.com.contabilidade.domain.repository.ContaRepository;
import br.com.contabilidade.domain.repository.LancamentoRepository;
import br.com.contabilidade.domain.vo.Competencia;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC-04 — Balancete do período (saldo anterior, movimento e saldo atual por conta, com agregação hierárquica). */
@Service
public class GerarBalanceteUseCase {
    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;

    public GerarBalanceteUseCase(ContaRepository contas, LancamentoRepository lancamentos) {
        this.contas = contas; this.lancamentos = lancamentos;
    }

    @Transactional(readOnly = true)
    public Balancete executar(Competencia competencia) {
        var todas = contas.listar(null, null);
        Map<UUID, Conta> porId = todas.stream().collect(Collectors.toMap(Conta::getId, Function.identity()));
        var anterior = Hierarquia.agregar(lancamentos.movimentos(LancamentoRepository.INICIO, competencia.inicio().minusDays(1)), porId);
        var periodo = Hierarquia.agregar(lancamentos.movimentos(competencia.inicio(), competencia.fim()), porId);

        var linhas = todas.stream()
                .filter(c -> anterior.containsKey(c.getId()) || periodo.containsKey(c.getId()))
                .map(c -> {
                    var ant = Hierarquia.de(anterior, c.getId());
                    var per = Hierarquia.de(periodo, c.getId());
                    return new BalanceteLinha(c.getId(), c.getCodigo().valor(), c.getNome(), c.getTipo(),
                            Saldo.de(ant.saldoLiquido()), per.debitos(), per.creditos(), Saldo.de(ant.somar(per).saldoLiquido()));
                }).toList();

        BigDecimal totalDebitos = BigDecimal.ZERO.setScale(2);
        BigDecimal totalCreditos = BigDecimal.ZERO.setScale(2);
        for (var c : todas) {
            if (c.getContaPaiId() == null && periodo.containsKey(c.getId())) {
                totalDebitos = totalDebitos.add(periodo.get(c.getId()).debitos());
                totalCreditos = totalCreditos.add(periodo.get(c.getId()).creditos());
            }
        }
        return new Balancete(competencia.toString(), linhas, totalDebitos, totalCreditos);
    }
}
