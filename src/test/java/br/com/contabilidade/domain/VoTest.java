package br.com.contabilidade.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.contabilidade.domain.enums.TipoConta;
import br.com.contabilidade.domain.enums.TipoPartida;
import br.com.contabilidade.domain.vo.CodigoConta;
import br.com.contabilidade.domain.vo.Competencia;
import br.com.contabilidade.domain.vo.Dinheiro;
import br.com.contabilidade.domain.vo.Moeda;
import br.com.contabilidade.domain.vo.NumeroLancamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class VoTest {

    @Test
    void dinheiroArredondaHalfEvenComEscala2() {
        assertThat(Dinheiro.brl(new BigDecimal("10.005")).valor()).isEqualByComparingTo("10.00");
        assertThat(Dinheiro.brl(new BigDecimal("10.015")).valor()).isEqualByComparingTo("10.02");
        assertThat(Dinheiro.brl(new BigDecimal("1")).valor().scale()).isEqualTo(2);
    }

    @Test
    void dinheiroSomaSubtraiEComparaZero() {
        var a = Dinheiro.brl(new BigDecimal("10.50"));
        var b = Dinheiro.brl(new BigDecimal("0.50"));
        assertThat(a.somar(b).valor()).isEqualByComparingTo("11.00");
        assertThat(a.subtrair(b).valor()).isEqualByComparingTo("10.00");
        assertThat(a.isPositivo()).isTrue();
        assertThat(Dinheiro.zero().isZero()).isTrue();
        assertThat(Dinheiro.zero().isPositivo()).isFalse();
    }

    @Test
    void competenciaNavegaEConverteTexto() {
        var jan = new Competencia(2025, 1);
        assertThat(jan.anterior()).isEqualTo(new Competencia(2024, 12));
        assertThat(new Competencia(2024, 12).proxima()).isEqualTo(jan);
        assertThat(jan.proxima()).isEqualTo(new Competencia(2025, 2));
        assertThat(jan.toString()).isEqualTo("2025-01");
        assertThat(Competencia.parse("2025-01")).isEqualTo(jan);
        assertThat(Competencia.of(LocalDate.of(2025, 1, 31))).isEqualTo(jan);
        assertThat(jan.inicio()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(jan.fim()).isEqualTo(LocalDate.of(2025, 1, 31));
        assertThat(jan.contem(LocalDate.of(2025, 1, 15))).isTrue();
        assertThat(jan).isLessThan(new Competencia(2025, 2));
        assertThatThrownBy(() -> new Competencia(2025, 13)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Competencia.parse("01/2025")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void codigoContaIdentificaPai() {
        var codigo = new CodigoConta("1.1.01");
        assertThat(codigo.isFilha()).isTrue();
        assertThat(codigo.pai()).contains(new CodigoConta("1.1"));
        assertThat(new CodigoConta("1").pai()).isEmpty();
        assertThat(new CodigoConta("1").isFilha()).isFalse();
        assertThatThrownBy(() -> new CodigoConta("1..1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CodigoConta("abc")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void numeroLancamentoFormataEFazParse() {
        var numero = new NumeroLancamento(new Competencia(2025, 1), 123);
        assertThat(numero.toString()).isEqualTo("2025-01-000123");
        assertThat(NumeroLancamento.parse("2025-01-000123")).isEqualTo(numero);
        assertThatThrownBy(() -> NumeroLancamento.parse("2025-01-1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NumeroLancamento(new Competencia(2025, 1), 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void enumsExpoemRegrasBasicas() {
        assertThat(TipoPartida.DEBITO.inverso()).isEqualTo(TipoPartida.CREDITO);
        assertThat(TipoPartida.CREDITO.inverso()).isEqualTo(TipoPartida.DEBITO);
        assertThat(TipoConta.ATIVO.naturezaPadrao().name()).isEqualTo("DEVEDORA");
        assertThat(TipoConta.RECEITA.naturezaPadrao().name()).isEqualTo("CREDORA");
        assertThat(Moeda.valueOf("BRL")).isEqualTo(Moeda.BRL);
    }
}
