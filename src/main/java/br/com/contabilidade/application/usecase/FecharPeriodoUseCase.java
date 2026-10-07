package br.com.contabilidade.application.usecase;

import br.com.contabilidade.domain.exception.RecursoNaoEncontradoException;
import br.com.contabilidade.domain.exception.RegraNegocioException;
import br.com.contabilidade.domain.model.PeriodoContabil;
import br.com.contabilidade.domain.repository.DomainEventPublisher;
import br.com.contabilidade.domain.repository.LancamentoRepository;
import br.com.contabilidade.domain.repository.PeriodoRepository;
import br.com.contabilidade.domain.vo.Competencia;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC-03 — Fechar período. RN-15: sem lançamentos pendentes (rascunhos). */
@Service
public class FecharPeriodoUseCase {
    private final PeriodoRepository periodos;
    private final LancamentoRepository lancamentos;
    private final DomainEventPublisher publisher;

    public FecharPeriodoUseCase(PeriodoRepository periodos, LancamentoRepository lancamentos, DomainEventPublisher publisher) {
        this.periodos = periodos; this.lancamentos = lancamentos; this.publisher = publisher;
    }

    @Transactional
    public PeriodoContabil executar(Competencia competencia, UUID usuarioId) {
        var periodo = periodos.buscarPorCompetencia(competencia)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Período", competencia));
        if (lancamentos.existeRascunho(competencia)) {
            throw new RegraNegocioException("RN-15", "Existem lançamentos pendentes (rascunho) em " + competencia);
        }
        periodo.fechar(usuarioId);
        var salvo = periodos.salvar(periodo);
        publisher.publicar(periodo.coletarEventos());
        return salvo;
    }
}
