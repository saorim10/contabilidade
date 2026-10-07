package br.com.contabilidade.application.usecase;

import br.com.contabilidade.domain.model.EventoAuditoria;
import br.com.contabilidade.domain.repository.EventoAuditoriaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-09 — trilha de auditoria. */
@Service
public class ConsultarAuditoriaUseCase {
    private final EventoAuditoriaRepository eventos;

    public ConsultarAuditoriaUseCase(EventoAuditoriaRepository eventos) { this.eventos = eventos; }

    @Transactional(readOnly = true)
    public List<EventoAuditoria> executar(UUID agregadoId, int pagina, int tamanho) {
        return eventos.listar(agregadoId, Math.max(0, pagina), Math.min(Math.max(1, tamanho), 200));
    }
}
