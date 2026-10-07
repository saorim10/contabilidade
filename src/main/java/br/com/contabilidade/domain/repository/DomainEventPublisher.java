package br.com.contabilidade.domain.repository;

import br.com.contabilidade.domain.event.DomainEvent;
import java.util.Collection;

public interface DomainEventPublisher {
    void publicar(Collection<DomainEvent> eventos);
}
