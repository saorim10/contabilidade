package br.com.contabilidade.domain.model;

import br.com.contabilidade.domain.enums.TipoPartida;
import br.com.contabilidade.domain.exception.RegraNegocioException;
import br.com.contabilidade.domain.vo.Dinheiro;
import java.util.Objects;
import java.util.UUID;

public record Partida(UUID id, UUID contaId, TipoPartida tipo, Dinheiro valor) {

    public Partida {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(contaId, "contaId");
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(valor, "valor");
        if (!valor.isPositivo()) throw new RegraNegocioException("RN-10", "Valor da partida deve ser maior que zero");
    }

    public static Partida nova(UUID contaId, TipoPartida tipo, Dinheiro valor) {
        return new Partida(UUID.randomUUID(), contaId, tipo, valor);
    }

    /** Partida com tipo invertido (usada no estorno). */
    public Partida inversa() { return nova(contaId, tipo.inverso(), valor); }
}
