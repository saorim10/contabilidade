package br.com.contabilidade.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.contabilidade.domain.enums.OrigemLancamento;
import br.com.contabilidade.domain.enums.StatusLancamento;
import br.com.contabilidade.domain.enums.TipoPartida;
import br.com.contabilidade.domain.exception.RegraNegocioException;
import br.com.contabilidade.domain.model.LancamentoContabil;
import br.com.contabilidade.domain.model.Partida;
import br.com.contabilidade.domain.vo.Competencia;
import br.com.contabilidade.domain.vo.Dinheiro;
import br.com.contabilidade.domain.vo.NumeroLancamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LancamentoContabilTest {
    static final UUID USUARIO = UUID.randomUUID();
    static final LocalDate DATA = LocalDate.of(2025, 1, 15);
    static final Competencia JAN = new Competencia(2025, 1);

    static Dinheiro r(String v) { return Dinheiro.brl(new BigDecimal(v)); }

    static Partida p(TipoPartida tipo, String valor) { return Partida.nova(UUID.randomUUID(), tipo, r(valor)); }

    static LancamentoContabil lancamento(List<Partida> partidas) {
        return LancamentoContabil.criar(new NumeroLancamento(JAN, 1), DATA, "Pagamento de fornecedor", OrigemLancamento.MANUAL, partidas, USUARIO);
    }

    static LancamentoContabil balanceado() {
        return lancamento(List.of(p(TipoPartida.DEBITO, "1500.00"), p(TipoPartida.CREDITO, "1500.00")));
    }

    @Test
    void confirmaLancamentoBalanceadoEPublicaEvento() {
        var l = balanceado();
        assertThat(l.getStatus()).isEqualTo(StatusLancamento.RASCUNHO);
        l.confirmar();
        assertThat(l.getStatus()).isEqualTo(StatusLancamento.CONFIRMADO);
        assertThat(l.totalDebitos()).isEqualTo(l.totalCreditos());
        assertThat(l.getCompetencia()).isEqualTo(JAN);

        var eventos = l.coletarEventos();
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).tipo()).isEqualTo("LancamentoCriado");
        assertThat(eventos.get(0).agregadoId()).isEqualTo(l.getId());
        assertThat(eventos.get(0).payload()).containsEntry("numero", "2025-01-000001");
        assertThat(l.coletarEventos()).isEmpty();
    }

    @Test
    void rn01_rejeitaPartidasDesbalanceadas() {
        var l = lancamento(List.of(p(TipoPartida.DEBITO, "100.00"), p(TipoPartida.CREDITO, "99.99")));
        assertThatThrownBy(l::confirmar).isInstanceOf(RegraNegocioException.class)
                .extracting(e -> ((RegraNegocioException) e).getRegra()).isEqualTo("RN-01");
    }

    @Test
    void rn02_exigeNoMinimoDuasPartidas() {
        var l = lancamento(List.of(p(TipoPartida.DEBITO, "100.00")));
        assertThatThrownBy(l::confirmar).isInstanceOf(RegraNegocioException.class)
                .extracting(e -> ((RegraNegocioException) e).getRegra()).isEqualTo("RN-02");
    }

    @Test
    void rn10_partidaDeveSerPositiva() {
        assertThatThrownBy(() -> p(TipoPartida.DEBITO, "0.00")).isInstanceOf(RegraNegocioException.class)
                .extracting(e -> ((RegraNegocioException) e).getRegra()).isEqualTo("RN-10");
        assertThatThrownBy(() -> p(TipoPartida.DEBITO, "-1")).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void rn13_confirmadoNaoAceitaNovasPartidasNemReconfirmacao() {
        var l = balanceado();
        l.confirmar();
        assertThatThrownBy(() -> l.adicionarPartida(p(TipoPartida.DEBITO, "1.00"))).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(l::confirmar).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void rascunhoAceitaPartidasAteConfirmar() {
        var l = lancamento(List.of(p(TipoPartida.DEBITO, "50.00")));
        l.adicionarPartida(p(TipoPartida.CREDITO, "50.00"));
        assertThat(l.getPartidas()).hasSize(2);
        l.confirmar();
        assertThat(l.getStatus()).isEqualTo(StatusLancamento.CONFIRMADO);
    }

    @Test
    void rn07_estornoInvertePartidasEMarcaOriginalComoEstornado() {
        var original = balanceado();
        original.confirmar();
        original.coletarEventos();

        var estorno = original.estornar("Valor lançado em duplicidade", new NumeroLancamento(JAN, 2), DATA, USUARIO);

        assertThat(original.getStatus()).isEqualTo(StatusLancamento.ESTORNADO);
        assertThat(estorno.getStatus()).isEqualTo(StatusLancamento.CONFIRMADO);
        assertThat(estorno.getOrigem()).isEqualTo(OrigemLancamento.ESTORNO);
        assertThat(estorno.getEstornoDeId()).isEqualTo(original.getId());
        assertThat(estorno.getHistorico()).contains("2025-01-000001").contains("duplicidade");
        for (int i = 0; i < original.getPartidas().size(); i++) {
            var o = original.getPartidas().get(i);
            var e = estorno.getPartidas().get(i);
            assertThat(e.contaId()).isEqualTo(o.contaId());
            assertThat(e.tipo()).isEqualTo(o.tipo().inverso());
            assertThat(e.valor()).isEqualTo(o.valor());
        }
        assertThat(original.coletarEventos()).extracting("class.simpleName").containsExactly("LancamentoEstornado");
        assertThat(original.coletarEventos()).isEmpty();
        assertThat(estorno.coletarEventos()).hasSize(1);
    }

    @Test
    void estornoExigeMotivoStatusConfirmadoENaoPodeSerEstornado() {
        var rascunho = balanceado();
        assertThatThrownBy(() -> rascunho.estornar("x", new NumeroLancamento(JAN, 2), DATA, USUARIO))
                .isInstanceOf(RegraNegocioException.class);

        var confirmado = balanceado();
        confirmado.confirmar();
        assertThatThrownBy(() -> confirmado.estornar(" ", new NumeroLancamento(JAN, 2), DATA, USUARIO))
                .isInstanceOf(RegraNegocioException.class);

        var estorno = confirmado.estornar("erro", new NumeroLancamento(JAN, 2), DATA, USUARIO);
        assertThatThrownBy(() -> estorno.estornar("de novo", new NumeroLancamento(JAN, 3), DATA, USUARIO))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> confirmado.estornar("outra vez", new NumeroLancamento(JAN, 4), DATA, USUARIO))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void validaDadosObrigatorios() {
        assertThatThrownBy(() -> LancamentoContabil.criar(new NumeroLancamento(JAN, 1), DATA, " ", OrigemLancamento.MANUAL, List.of(), USUARIO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LancamentoContabil.criar(new NumeroLancamento(new Competencia(2025, 2), 1), DATA, "x",
                OrigemLancamento.MANUAL, List.of(), USUARIO)).isInstanceOf(IllegalArgumentException.class);
    }
}
