package br.com.contabilidade.application.relatorio;

import br.com.contabilidade.application.relatorio.Relatorios.LinhaSaldo;
import br.com.contabilidade.domain.enums.TipoConta;
import br.com.contabilidade.domain.model.Conta;
import br.com.contabilidade.domain.model.MovimentoConta;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Agrega o movimento das contas analíticas nas contas sintéticas ancestrais. */
final class Hierarquia {
    private Hierarquia() {}

    static Map<UUID, MovimentoConta> agregar(List<MovimentoConta> movimentos, Map<UUID, Conta> contas) {
        Map<UUID, MovimentoConta> resultado = new HashMap<>();
        for (var m : movimentos) {
            UUID atual = m.contaId();
            while (atual != null) {
                resultado.merge(atual, new MovimentoConta(atual, m.debitos(), m.creditos()), MovimentoConta::somar);
                Conta conta = contas.get(atual);
                atual = conta == null ? null : conta.getContaPaiId();
            }
        }
        return resultado;
    }

    static MovimentoConta de(Map<UUID, MovimentoConta> agregado, UUID contaId) {
        return agregado.getOrDefault(contaId, MovimentoConta.zero(contaId));
    }

    /** Linhas com saldo no sentido natural do tipo (receita/passivo/PL = créditos - débitos). */
    static List<LinhaSaldo> linhas(TipoConta tipo, List<Conta> contas, Map<UUID, MovimentoConta> agregado) {
        return contas.stream()
                .filter(c -> c.getTipo() == tipo && agregado.containsKey(c.getId()))
                .map(c -> new LinhaSaldo(c.getId(), c.getCodigo().valor(), c.getNome(),
                        agregado.get(c.getId()).saldo(tipo.naturezaPadrao())))
                .toList();
    }

    /** Total do tipo = soma das contas raiz (já agregadas). */
    static BigDecimal total(TipoConta tipo, List<Conta> contas, Map<UUID, MovimentoConta> agregado) {
        return contas.stream()
                .filter(c -> c.getTipo() == tipo && c.getContaPaiId() == null)
                .map(c -> de(agregado, c.getId()).saldo(tipo.naturezaPadrao()))
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
