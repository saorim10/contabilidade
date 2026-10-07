package br.com.contabilidade.application.usecase;

import br.com.contabilidade.domain.exception.RecursoNaoEncontradoException;
import br.com.contabilidade.domain.model.LancamentoContabil;
import br.com.contabilidade.domain.repository.LancamentoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarLancamentoUseCase {
    private final LancamentoRepository lancamentos;

    public ConsultarLancamentoUseCase(LancamentoRepository lancamentos) { this.lancamentos = lancamentos; }

    @Transactional(readOnly = true)
    public LancamentoContabil executar(UUID id) {
        return lancamentos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Lançamento", id));
    }
}
