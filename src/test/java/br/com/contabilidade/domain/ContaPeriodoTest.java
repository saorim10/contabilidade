package br.com.contabilidade.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.contabilidade.domain.enums.NaturezaSaldo;
import br.com.contabilidade.domain.enums.StatusPeriodo;
import br.com.contabilidade.domain.enums.TipoConta;
import br.com.contabilidade.domain.enums.TipoPartida;
import br.com.contabilidade.domain.event.DomainEvent;
import br.com.contabilidade.domain.exception.RecursoNaoEncontradoException;
import br.com.contabilidade.domain.exception.RegraNegocioException;
import br.com.contabilidade.domain.model.Conta;
import br.com.contabilidade.domain.model.EventoAuditoria;
import br.com.contabilidade.domain.model.LinhaRazao;
import br.com.contabilidade.domain.model.MovimentoConta;
import br.com.contabilidade.domain.model.PeriodoContabil;
import br.com.contabilidade.domain.model.SugestaoConta;
import br.com.contabilidade.domain.vo.CodigoConta;
import br.com.contabilidade.domain.vo.Competencia;
import br.com.contabilidade.domain.vo.Dinheiro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ContaPeriodoTest {
    static final UUID USUARIO = UUID.randomUUID();

    static Conta caixa() { return Conta.criar(new CodigoConta("1.1.01"), "Caixa", TipoConta.ATIVO, null, null); }

    @Test
    void contaAssumeNaturezaPadraoDoTipo() {
        assertThat(caixa().getNatureza()).isEqualTo(NaturezaSaldo.DEVEDORA);
        assertThat(Conta.criar(new CodigoConta("2.1.01"), "Fornecedores", TipoConta.PASSIVO, null, null).getNatureza())
                .isEqualTo(NaturezaSaldo.CREDORA);
        assertThat(Conta.criar(new CodigoConta("1.1.09"), "(-) Perdas", TipoConta.ATIVO, NaturezaSaldo.CREDORA, null).getNatureza())
                .isEqualTo(NaturezaSaldo.CREDORA);
    }

    @Test
    void rn08_inativaSomenteComSaldoZero() {
        var conta = caixa();
        assertThat(conta.podeReceberLancamento()).isTrue();
        assertThatThrownBy(() -> conta.inativar(Dinheiro.brl(new BigDecimal("10.00")), USUARIO))
                .isInstanceOf(RegraNegocioException.class)
                .extracting(e -> ((RegraNegocioException) e).getRegra()).isEqualTo("RN-08");

        conta.inativar(Dinheiro.zero(), USUARIO);
        assertThat(conta.isAtiva()).isFalse();
        assertThat(conta.podeReceberLancamento()).isFalse();
        var eventos = conta.coletarEventos();
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).tipo()).isEqualTo("ContaInativada");
        assertThat(eventos.get(0).payload()).containsEntry("codigo", "1.1.01");
        assertThatThrownBy(() -> conta.inativar(Dinheiro.zero(), USUARIO)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void contaValidaNome() {
        assertThatThrownBy(() -> Conta.criar(new CodigoConta("1"), " ", TipoConta.ATIVO, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Conta.criar(new CodigoConta("1"), "x".repeat(121), TipoConta.ATIVO, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void periodoSegueOCicloDeVida() {
        var periodo = PeriodoContabil.abrir(new Competencia(2025, 1));
        assertThat(periodo.aceitaLancamento()).isTrue();

        periodo.fechar(USUARIO);
        assertThat(periodo.getStatus()).isEqualTo(StatusPeriodo.FECHADO);
        assertThat(periodo.getFechadoPor()).isEqualTo(USUARIO);
        assertThat(periodo.getFechadoEm()).isNotNull();
        assertThat(periodo.aceitaLancamento()).isFalse();

        periodo.bloquear();
        assertThat(periodo.getStatus()).isEqualTo(StatusPeriodo.BLOQUEADO);
        periodo.desbloquear();
        assertThat(periodo.getStatus()).isEqualTo(StatusPeriodo.FECHADO);
        periodo.reabrir();
        assertThat(periodo.getStatus()).isEqualTo(StatusPeriodo.ABERTO);
        assertThat(periodo.getFechadoEm()).isNull();

        var eventos = periodo.coletarEventos();
        assertThat(eventos).extracting(DomainEvent::tipo).containsExactly("PeriodoFechado");
        assertThat(eventos.get(0).payload()).containsEntry("competencia", "2025-01");
    }

    @Test
    void periodoRejeitaTransicoesInvalidas() {
        var periodo = PeriodoContabil.abrir(new Competencia(2025, 1));
        assertThatThrownBy(periodo::bloquear).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(periodo::reabrir).isInstanceOf(RegraNegocioException.class);
        periodo.fechar(USUARIO);
        assertThatThrownBy(() -> periodo.fechar(USUARIO)).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(periodo::desbloquear).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void movimentoCalculaSaldoPorNatureza() {
        var id = UUID.randomUUID();
        var m = new MovimentoConta(id, new BigDecimal("100.00"), new BigDecimal("30.00"));
        assertThat(m.saldoLiquido()).isEqualByComparingTo("70.00");
        assertThat(m.saldo(NaturezaSaldo.DEVEDORA)).isEqualByComparingTo("70.00");
        assertThat(m.saldo(NaturezaSaldo.CREDORA)).isEqualByComparingTo("-70.00");
        assertThat(m.somar(MovimentoConta.zero(id)).debitos()).isEqualByComparingTo("100.00");
    }

    @Test
    void eventoDeAuditoriaEspelhaOEventoDeDominio() {
        var conta = caixa();
        conta.inativar(Dinheiro.zero(), USUARIO);
        var evento = conta.coletarEventos().get(0);
        var auditoria = EventoAuditoria.de(evento);
        assertThat(auditoria.tipoEvento()).isEqualTo("ContaInativada");
        assertThat(auditoria.agregadoId()).isEqualTo(conta.getId());
        assertThat(auditoria.usuarioId()).isEqualTo(USUARIO);
        assertThat(auditoria.id()).isNotNull();
    }

    @Test
    void recordsEExcecoesAuxiliares() {
        assertThat(new LinhaRazao(LocalDate.of(2025, 1, 1), "n", "h", TipoPartida.DEBITO, BigDecimal.ONE).valor()).isEqualTo(BigDecimal.ONE);
        assertThat(new SugestaoConta(UUID.randomUUID(), "1", "x", 0.5).confianca()).isEqualTo(0.5);
        assertThat(new RecursoNaoEncontradoException("Conta", 7).getMessage()).contains("Conta").contains("7");
    }
}
