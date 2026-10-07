package br.com.contabilidade.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Valor monetário: BigDecimal com escala 2 e arredondamento HALF_EVEN (RN-10). */
public record Dinheiro(BigDecimal valor, Moeda moeda) {
    public static final int ESCALA = 2;

    public Dinheiro {
        Objects.requireNonNull(valor, "valor");
        Objects.requireNonNull(moeda, "moeda");
        valor = valor.setScale(ESCALA, RoundingMode.HALF_EVEN);
    }

    public static Dinheiro brl(BigDecimal valor) { return new Dinheiro(valor, Moeda.BRL); }

    public static Dinheiro zero() { return brl(BigDecimal.ZERO); }

    public Dinheiro somar(Dinheiro outro) {
        mesmaMoeda(outro);
        return new Dinheiro(valor.add(outro.valor), moeda);
    }

    public Dinheiro subtrair(Dinheiro outro) {
        mesmaMoeda(outro);
        return new Dinheiro(valor.subtract(outro.valor), moeda);
    }

    public boolean isPositivo() { return valor.signum() > 0; }

    public boolean isZero() { return valor.signum() == 0; }

    private void mesmaMoeda(Dinheiro outro) {
        if (moeda != outro.moeda) throw new IllegalArgumentException("Moedas diferentes: " + moeda + " e " + outro.moeda);
    }
}
