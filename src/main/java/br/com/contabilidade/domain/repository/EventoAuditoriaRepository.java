package br.com.contabilidade.domain.repository;

import br.com.contabilidade.domain.model.EventoAuditoria;
import java.util.List;
import java.util.UUID;

public interface EventoAuditoriaRepository {
    void salvar(EventoAuditoria evento);
    /** Mais recentes primeiro. agregadoId opcional. */
    List<EventoAuditoria> listar(UUID agregadoId, int pagina, int tamanho);
}
