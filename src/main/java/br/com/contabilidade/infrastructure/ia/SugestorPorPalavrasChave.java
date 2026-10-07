package br.com.contabilidade.infrastructure.ia;

import br.com.contabilidade.application.port.SugestorDeConta;
import br.com.contabilidade.domain.model.Conta;
import br.com.contabilidade.domain.model.SugestaoConta;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Adaptador de IA (stub): casa palavras da descrição com o nome das contas.
 * Substitua por uma chamada a um LLM mantendo a mesma porta; a sugestão continua não autoritativa.
 */
@Component
public class SugestorPorPalavrasChave implements SugestorDeConta {

    @Override
    public Optional<SugestaoConta> sugerir(String descricao, List<Conta> candidatas) {
        Set<String> palavras = tokenizar(descricao);
        if (palavras.isEmpty()) return Optional.empty();

        Conta melhor = null;
        double melhorScore = 0;
        for (Conta c : candidatas) {
            Set<String> nome = tokenizar(c.getNome());
            long acertos = palavras.stream()
                    .filter(p -> nome.stream().anyMatch(n -> n.startsWith(p) || p.startsWith(n)))
                    .count();
            double score = (double) acertos / palavras.size();
            if (score > melhorScore) { melhorScore = score; melhor = c; }
        }
        if (melhor == null) return Optional.empty();
        double confianca = Math.round(Math.min(0.95, melhorScore) * 100.0) / 100.0;
        return Optional.of(new SugestaoConta(melhor.getId(), melhor.getCodigo().valor(), melhor.getNome(), confianca));
    }

    private Set<String> tokenizar(String texto) {
        if (texto == null) return Set.of();
        String limpo = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
        return Arrays.stream(limpo.split("[^a-z]+")).filter(t -> t.length() > 3).collect(Collectors.toSet());
    }
}
