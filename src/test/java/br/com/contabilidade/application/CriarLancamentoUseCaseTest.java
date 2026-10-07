package br.com.contabilidade.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.contabilidade.application.usecase.CriarLancamentoUseCase;
import br.com.contabilidade.application.usecase.CriarLancamentoUseCase.Command;
import br.com.contabilidade.application.usecase.CriarLancamentoUseCase.PartidaCommand;
import br.com.contabilidade.application.usecase.ValidadorPeriodo;
import br.com.contabilidade.domain.enums.OrigemLancamento;
import br.com.contabilidade.domain.enums.StatusLancamento;
import br.com.contabilidade.domain.enums.TipoConta;
import br.com.contabilidade.domain.enums.TipoPartida;
import br.com.contabilidade.domain.exception.RegraNegocioException;
import br.com.contabilidade.domain.model.Conta;
import br.com.contabilidade.domain.model.LancamentoContabil;
import br.com.contabilidade.domain.model.PeriodoContabil;
import br.com.contabilidade.domain.repository.ContaRepository;
import br.com.contabilidade.domain.repository.DomainEventPublisher;
import br.com.contabilidade.domain.repository.GeradorNumeroLancamento;
import br.com.contabilidade.domain.repository.LancamentoRepository;
import br.com.contabilidade.domain.repository.PeriodoRepository;
import br.com.contabilidade.domain.vo.CodigoConta;
import br.com.contabilidade.domain.vo.Competencia;
import br.com.contabilidade.domain.vo.Dinheiro;
import br.com.contabilidade.domain.vo.NumeroLancamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CriarLancamentoUseCaseTest {
    static final Competencia JAN = new Competencia(2025, 1);
    static final LocalDate DATA = LocalDate.of(2025, 1, 15);

    @Mock LancamentoRepository lancamentos;
    @Mock ContaRepository contas;
    @Mock PeriodoRepository periodos;
    @Mock GeradorNumeroLancamento gerador;
    @Mock DomainEventPublisher publisher;

    CriarLancamentoUseCase useCase;
    Conta fornecedores = Conta.criar(new CodigoConta("2.1.01"), "Fornecedores", TipoConta.PASSIVO, null, null);
    Conta banco = Conta.criar(new CodigoConta("1.1.02"), "Bancos", TipoConta.ATIVO, null, null);

    @BeforeEach
    void setUp() {
        useCase = new CriarLancamentoUseCase(lancamentos, contas, new ValidadorPeriodo(periodos), gerador, publisher);
        when(periodos.buscarPorCompetencia(JAN)).thenReturn(Optional.of(PeriodoContabil.abrir(JAN)));
        when(contas.buscarPorIds(any())).thenReturn(List.of(fornecedores, banco));
        when(contas.possuiFilhas(any())).thenReturn(false);
        when(gerador.proximo(JAN)).thenReturn(new NumeroLancamento(JAN, 1));
        when(lancamentos.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    Command comando(String debito, String credito) {
        return new Command(DATA, "Pagamento de fornecedor XYZ", OrigemLancamento.MANUAL, List.of(
                new PartidaCommand(fornecedores.getId(), TipoPartida.DEBITO, new BigDecimal(debito)),
                new PartidaCommand(banco.getId(), TipoPartida.CREDITO, new BigDecimal(credito))), UUID.randomUUID());
    }

    @Test
    void criaLancamentoConfirmadoEPublicaEventos() {
        LancamentoContabil l = useCase.executar(comando("1500.00", "1500.00"));
        assertThat(l.getStatus()).isEqualTo(StatusLancamento.CONFIRMADO);
        assertThat(l.getNumero().toString()).isEqualTo("2025-01-000001");
        verify(publisher).publicar(any());
    }

    @Test
    void naoPersisteLancamentoDesbalanceado() {
        assertThatThrownBy(() -> useCase.executar(comando("1500.00", "1499.99")))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("débitos");
        verify(lancamentos, never()).salvar(any());
        verify(publisher, never()).publicar(any());
    }

    @Test
    void rn04_rejeitaContaInativa() {
        banco.inativar(Dinheiro.zero(), UUID.randomUUID());
        assertThatThrownBy(() -> useCase.executar(comando("10.00", "10.00")))
                .isInstanceOf(RegraNegocioException.class)
                .extracting(e -> ((RegraNegocioException) e).getRegra()).isEqualTo("RN-04");
    }

    @Test
    void rn09_rejeitaContaSintetica() {
        when(contas.possuiFilhas(fornecedores.getId())).thenReturn(true);
        assertThatThrownBy(() -> useCase.executar(comando("10.00", "10.00")))
                .isInstanceOf(RegraNegocioException.class)
                .extracting(e -> ((RegraNegocioException) e).getRegra()).isEqualTo("RN-09");
    }

    @Test
    void rn05_rejeitaPeriodoFechado() {
        var fechado = PeriodoContabil.abrir(JAN);
        fechado.fechar(UUID.randomUUID());
        when(periodos.buscarPorCompetencia(JAN)).thenReturn(Optional.of(fechado));
        assertThatThrownBy(() -> useCase.executar(comando("10.00", "10.00")))
                .isInstanceOf(RegraNegocioException.class)
                .extracting(e -> ((RegraNegocioException) e).getRegra()).isEqualTo("RN-05");
    }

    @Test
    void abrePeriodoInexistenteSobDemanda() {
        when(periodos.buscarPorCompetencia(JAN)).thenReturn(Optional.empty());
        when(periodos.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(useCase.executar(comando("10.00", "10.00")).getStatus()).isEqualTo(StatusLancamento.CONFIRMADO);
        verify(periodos).salvar(any());
    }

    @Test
    void naoPermiteOrigemEstornoPelaApi() {
        var cmd = new Command(DATA, "x", OrigemLancamento.ESTORNO, List.of(), UUID.randomUUID());
        assertThatThrownBy(() -> useCase.executar(cmd)).isInstanceOf(RegraNegocioException.class);
    }
}
