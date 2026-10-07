package br.com.contabilidade.domain.repository;

import br.com.contabilidade.domain.enums.NaturezaSaldo;
import br.com.contabilidade.domain.enums.TipoConta;
import br.com.contabilidade.domain.model.Conta;
import br.com.contabilidade.domain.vo.CodigoConta;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaRepository {
    Conta salvar(Conta conta);
    Optional<Conta> buscarPorId(UUID id);
    Optional<Conta> buscarPorCodigo(CodigoConta codigo);
    List<Conta> buscarPorIds(Collection<UUID> ids);
    /** Filtros opcionais (null = sem filtro). Ordenado por código. */
    List<Conta> listar(TipoConta tipo, NaturezaSaldo natureza);
    boolean possuiFilhas(UUID contaId);
}
