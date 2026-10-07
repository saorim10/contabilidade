package br.com.contabilidade.infrastructure.event;

import br.com.contabilidade.domain.event.DomainEvent;
import br.com.contabilidade.domain.model.EventoAuditoria;
import br.com.contabilidade.domain.repository.DomainEventPublisher;
import br.com.contabilidade.domain.repository.EventoAuditoriaRepository;
import java.util.Collection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Persiste cada evento na trilha de auditoria (mesma transação = outbox) e o publica via ApplicationEventPublisher.
 * Uma futura integração Kafka/RabbitMQ pode ler a tabela evento_auditoria ou ouvir estes eventos.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
    private final EventoAuditoriaRepository auditoria;
    private final ApplicationEventPublisher spring;

    public SpringDomainEventPublisher(EventoAuditoriaRepository auditoria, ApplicationEventPublisher spring) {
        this.auditoria = auditoria; this.spring = spring;
    }

    @Override
    public void publicar(Collection<DomainEvent> eventos) {
        for (DomainEvent e : eventos) {
            auditoria.salvar(EventoAuditoria.de(e));
            spring.publishEvent(e);
        }
    }
}
