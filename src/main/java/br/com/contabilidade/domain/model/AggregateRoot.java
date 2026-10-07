package br.com.contabilidade.domain.model;

import br.com.contabilidade.domain.event.DomainEvent;
import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot {
    private final List<DomainEvent> eventos = new ArrayList<>();

    protected void registrar(DomainEvent evento) { eventos.add(evento); }

    /** Retorna e limpa os eventos pendentes de publicação. */
    public List<DomainEvent> coletarEventos() {
        var copia = List.copyOf(eventos);
        eventos.clear();
        return copia;
    }
}
