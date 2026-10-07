package br.com.contabilidade.application.usecase;

import br.com.contabilidade.application.port.SugestorDeConta;
import br.com.contabilidade.domain.model.Conta;
import br.com.contabilidade.domain.model.SugestaoConta;
import br.com.contabilidade.domain.repository.ContaRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC-05 / RF-10 — sugestão NÃO autoritativa: nada é persistido (RN-11). */
@Service
public class SugerirContaUseCase {
    private final ContaRepository contas;
    private final SugestorDeConta sugestor;

    public SugerirContaUseCase(ContaRepository contas, SugestorDeConta sugestor) {
        this.contas = contas; this.sugestor = sugestor;
    }

    @Transactional(readOnly = true)
    public Optional<SugestaoConta> executar(String descricao) {
        var todas = contas.listar(null, null);
        Set<UUID> pais = todas.stream().map(Conta::getContaPaiId).filter(Objects::nonNull).collect(Collectors.toSet());
        var candidatas = todas.stream().filter(c -> c.isAtiva() && !pais.contains(c.getId())).toList(); // apenas analíticas ativas
        return sugestor.sugerir(descricao, candidatas);
    }
}
