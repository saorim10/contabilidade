package br.com.contabilidade.application.port;

import br.com.contabilidade.domain.model.Conta;
import br.com.contabilidade.domain.model.SugestaoConta;
import java.util.List;
import java.util.Optional;

/** Porta para assistentes de IA. A sugestão é sempre não autoritativa (RN-11). */
public interface SugestorDeConta {
    Optional<SugestaoConta> sugerir(String descricao, List<Conta> candidatas);
}
