package br.com.contabilidade.domain.model;

import br.com.contabilidade.domain.enums.NaturezaSaldo;
import java.math.BigDecimal;
import java.util.UUID;

/** Totais de débitos e créditos de uma conta. */
public record MovimentoConta(UUID contaId, BigDecimal debitos, BigDecimal creditos) {

    public static MovimentoConta zero(UUID contaId) {
        var zero = BigDecimal.ZERO.setScale(2);
        return new MovimentoConta(contaId, zero, zero);
    }

    public MovimentoConta somar(MovimentoConta outro) {
        return new MovimentoConta(contaId, debitos.add(outro.debitos), creditos.add(outro.creditos));
    }

    /** Débitos - créditos. */
    public BigDecimal saldoLiquido() { return debitos.subtract(creditos); }

    /** Saldo no sentido da natureza informada (positivo = saldo "normal" da natureza). */
    public BigDecimal saldo(NaturezaSaldo natureza) {
        return natureza == NaturezaSaldo.DEVEDORA ? saldoLiquido() : saldoLiquido().negate();
    }
}
