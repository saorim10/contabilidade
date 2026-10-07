package br.com.contabilidade.domain.vo;

import java.util.Objects;

/** Numeração sequencial por competência (RN-14), ex.: 2025-01-000123. */
public record NumeroLancamento(Competencia competencia, int sequencial) {

    public NumeroLancamento {
        Objects.requireNonNull(competencia, "competencia");
        if (sequencial < 1) throw new IllegalArgumentException("Sequencial deve ser >= 1");
    }

    public static NumeroLancamento parse(String texto) {
        if (texto == null || !texto.matches("\\d{4}-\\d{2}-\\d{6,}")) {
            throw new IllegalArgumentException("Número de lançamento inválido: " + texto);
        }
        return new NumeroLancamento(Competencia.parse(texto.substring(0, 7)), Integer.parseInt(texto.substring(8)));
    }

    @Override public String toString() { return String.format("%s-%06d", competencia, sequencial); }
}
