package br.com.contabilidade.domain.repository;

import br.com.contabilidade.domain.model.LancamentoContabil;
import br.com.contabilidade.domain.model.LinhaRazao;
import br.com.contabilidade.domain.model.MovimentoConta;
import br.com.contabilidade.domain.vo.Competencia;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LancamentoRepository {
    LocalDate INICIO = LocalDate.of(1900, 1, 1);
    LocalDate FIM = LocalDate.of(9999, 12, 31);

    /** Insere (novo) ou atualiza somente o status (existente). */
    LancamentoContabil salvar(LancamentoContabil lancamento);
    Optional<LancamentoContabil> buscarPorId(UUID id);
    /** Movimento por conta (analítica) no intervalo, ignorando rascunhos. */
    List<MovimentoConta> movimentos(LocalDate inicio, LocalDate fim);
    /** Movimento acumulado de uma conta até a data (inclusive). Nunca nulo. */
    MovimentoConta movimentoDaConta(UUID contaId, LocalDate ate);
    List<LinhaRazao> razao(UUID contaId, LocalDate inicio, LocalDate fim);
    boolean existeRascunho(Competencia competencia);
}
