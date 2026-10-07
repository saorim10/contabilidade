package br.com.contabilidade.domain.vo;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/** Competência contábil (ano/mês), ex.: 2025-01. */
public record Competencia(int ano, int mes) implements Comparable<Competencia> {

    public Competencia {
        if (mes < 1 || mes > 12) throw new IllegalArgumentException("Mês inválido: " + mes);
        if (ano < 1900 || ano > 9999) throw new IllegalArgumentException("Ano inválido: " + ano);
    }

    public static Competencia of(LocalDate data) { return new Competencia(data.getYear(), data.getMonthValue()); }

    public static Competencia parse(String texto) {
        if (texto == null || !texto.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException("Competência inválida (esperado AAAA-MM): " + texto);
        }
        return new Competencia(Integer.parseInt(texto.substring(0, 4)), Integer.parseInt(texto.substring(5, 7)));
    }

    public Competencia proxima() { return mes == 12 ? new Competencia(ano + 1, 1) : new Competencia(ano, mes + 1); }

    public Competencia anterior() { return mes == 1 ? new Competencia(ano - 1, 12) : new Competencia(ano, mes - 1); }

    public LocalDate inicio() { return LocalDate.of(ano, mes, 1); }

    public LocalDate fim() { return inicio().with(TemporalAdjusters.lastDayOfMonth()); }

    public boolean contem(LocalDate data) { return equals(of(data)); }

    @Override public int compareTo(Competencia o) { return ano != o.ano ? Integer.compare(ano, o.ano) : Integer.compare(mes, o.mes); }

    @Override public String toString() { return String.format("%04d-%02d", ano, mes); }
}
