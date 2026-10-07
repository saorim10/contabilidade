package br.com.contabilidade.application.relatorio;

import br.com.contabilidade.domain.enums.NaturezaSaldo;
import br.com.contabilidade.domain.enums.TipoConta;
import br.com.contabilidade.domain.enums.TipoPartida;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Modelos de leitura dos relatórios contábeis. */
public final class Relatorios {
    private Relatorios() {}

    /** Saldo em módulo + natureza (devedor/credor). */
    public record Saldo(BigDecimal valor, NaturezaSaldo natureza) {
        public static Saldo de(BigDecimal debitoMenosCredito) {
            return new Saldo(debitoMenosCredito.abs().setScale(2, RoundingMode.HALF_EVEN),
                    debitoMenosCredito.signum() >= 0 ? NaturezaSaldo.DEVEDORA : NaturezaSaldo.CREDORA);
        }
    }

    public record BalanceteLinha(UUID contaId, String codigo, String nome, TipoConta tipo, Saldo saldoAnterior,
                                 BigDecimal debitos, BigDecimal creditos, Saldo saldoAtual) {}

    public record Balancete(String competencia, List<BalanceteLinha> linhas, BigDecimal totalDebitos, BigDecimal totalCreditos) {}

    public record LinhaSaldo(UUID contaId, String codigo, String nome, BigDecimal saldo) {}

    public record Balanco(String competencia, List<LinhaSaldo> ativo, List<LinhaSaldo> passivo,
                          List<LinhaSaldo> patrimonioLiquido, BigDecimal totalAtivo, BigDecimal totalPassivo,
                          BigDecimal totalPatrimonioLiquido, BigDecimal resultadoAcumulado,
                          BigDecimal totalPassivoMaisPatrimonio, boolean balanceado) {}

    public record Dre(String competenciaInicial, String competenciaFinal, List<LinhaSaldo> receitas, List<LinhaSaldo> despesas,
                      BigDecimal totalReceitas, BigDecimal totalDespesas, BigDecimal resultado) {}

    public record LinhaRazaoSaldo(LocalDate data, String numero, String historico, TipoPartida tipo,
                                  BigDecimal valor, BigDecimal saldoAcumulado) {}

    public record Razao(UUID contaId, String codigo, String nome, String competencia, BigDecimal saldoInicial,
                        List<LinhaRazaoSaldo> linhas, BigDecimal saldoFinal) {}
}
