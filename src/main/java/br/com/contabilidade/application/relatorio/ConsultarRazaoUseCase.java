package br.com.contabilidade.application.relatorio;

import br.com.contabilidade.application.relatorio.Relatorios.LinhaRazaoSaldo;
import br.com.contabilidade.application.relatorio.Relatorios.Razao;
import br.com.contabilidade.domain.enums.NaturezaSaldo;
import br.com.contabilidade.domain.enums.TipoPartida;
import br.com.contabilidade.domain.exception.RecursoNaoEncontradoException;
import br.com.contabilidade.domain.repository.ContaRepository;
import br.com.contabilidade.domain.repository.LancamentoRepository;
import br.com.contabilidade.domain.vo.Competencia;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC-06 / RF-08 — Razão analítico de uma conta em uma competência, com saldo acumulado. */
@Service
public class ConsultarRazaoUseCase {
    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;

    public ConsultarRazaoUseCase(ContaRepository contas, LancamentoRepository lancamentos) {
        this.contas = contas; this.lancamentos = lancamentos;
    }

    @Transactional(readOnly = true)
    public Razao executar(UUID contaId, Competencia competencia) {
        var conta = contas.buscarPorId(contaId).orElseThrow(() -> new RecursoNaoEncontradoException("Conta", contaId));
        BigDecimal saldo = lancamentos.movimentoDaConta(contaId, competencia.inicio().minusDays(1)).saldo(conta.getNatureza());
        BigDecimal saldoInicial = saldo;

        var linhas = new ArrayList<LinhaRazaoSaldo>();
        for (var l : lancamentos.razao(contaId, competencia.inicio(), competencia.fim())) {
            boolean aumenta = (l.tipo() == TipoPartida.DEBITO) == (conta.getNatureza() == NaturezaSaldo.DEVEDORA);
            saldo = aumenta ? saldo.add(l.valor()) : saldo.subtract(l.valor());
            linhas.add(new LinhaRazaoSaldo(l.data(), l.numero(), l.historico(), l.tipo(), l.valor(), saldo));
        }
        return new Razao(conta.getId(), conta.getCodigo().valor(), conta.getNome(), competencia.toString(), saldoInicial, linhas, saldo);
    }
}
