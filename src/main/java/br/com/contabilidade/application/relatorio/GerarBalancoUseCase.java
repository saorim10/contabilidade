package br.com.contabilidade.application.relatorio;

import br.com.contabilidade.application.relatorio.Relatorios.Balanco;
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

/**
 * RF-06 — Balanço patrimonial acumulado até o fim da competência.
 * O resultado acumulado (receitas - despesas) é somado ao lado do passivo + PL, pois não há apuração/encerramento de resultado.
 */
@Service
public class GerarBalancoUseCase {
    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;

    public GerarBalancoUseCase(ContaRepository contas, LancamentoRepository lancamentos) {
        this.contas = contas; this.lancamentos = lancamentos;
    }

    @Transactional(readOnly = true)
    public Balanco executar(Competencia competencia) {
        var todas = contas.listar(null, null);
        Map<UUID, Conta> porId = todas.stream().collect(Collectors.toMap(Conta::getId, Function.identity()));
        var acumulado = Hierarquia.agregar(lancamentos.movimentos(LancamentoRepository.INICIO, competencia.fim()), porId);

        var totalAtivo = Hierarquia.total(TipoConta.ATIVO, todas, acumulado);
        var totalPassivo = Hierarquia.total(TipoConta.PASSIVO, todas, acumulado);
        var totalPl = Hierarquia.total(TipoConta.PATRIMONIO_LIQUIDO, todas, acumulado);
        var resultado = Hierarquia.total(TipoConta.RECEITA, todas, acumulado).subtract(Hierarquia.total(TipoConta.DESPESA, todas, acumulado));
        var passivoMaisPl = totalPassivo.add(totalPl).add(resultado);

        return new Balanco(competencia.toString(),
                Hierarquia.linhas(TipoConta.ATIVO, todas, acumulado),
                Hierarquia.linhas(TipoConta.PASSIVO, todas, acumulado),
                Hierarquia.linhas(TipoConta.PATRIMONIO_LIQUIDO, todas, acumulado),
                totalAtivo, totalPassivo, totalPl, resultado, passivoMaisPl, totalAtivo.compareTo(passivoMaisPl) == 0);
    }
}
