package br.com.contabilidade.infrastructure.persistence;

import br.com.contabilidade.domain.model.EventoAuditoria;
import br.com.contabilidade.domain.repository.EventoAuditoriaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class EventoAuditoriaRepositoryAdapter implements EventoAuditoriaRepository {
    private final EventoAuditoriaJpaRepository jpa;

    public EventoAuditoriaRepositoryAdapter(EventoAuditoriaJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public void salvar(EventoAuditoria ev) {
        var e = new EventoAuditoriaJpa();
        e.id = ev.id(); e.tipoEvento = ev.tipoEvento(); e.agregadoId = ev.agregadoId();
        e.payload = ev.payload(); e.ocorridoEm = ev.ocorridoEm(); e.usuarioId = ev.usuarioId();
        jpa.save(e);
    }

    @Override
    public List<EventoAuditoria> listar(UUID agregadoId, int pagina, int tamanho) {
        var pageable = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "ocorridoEm"));
        var page = agregadoId == null ? jpa.findAll(pageable) : jpa.findByAgregadoId(agregadoId, pageable);
        return page.stream()
                .map(e -> new EventoAuditoria(e.id, e.tipoEvento, e.agregadoId, e.payload, e.ocorridoEm, e.usuarioId))
                .toList();
    }
}
