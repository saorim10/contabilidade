package br.com.contabilidade.infrastructure.persistence;

import br.com.contabilidade.domain.enums.NaturezaSaldo;
import br.com.contabilidade.domain.enums.TipoConta;
import br.com.contabilidade.domain.model.Conta;
import br.com.contabilidade.domain.repository.ContaRepository;
import br.com.contabilidade.domain.vo.CodigoConta;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class ContaRepositoryAdapter implements ContaRepository {
    private final ContaJpaRepository jpa;

    public ContaRepositoryAdapter(ContaJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public Conta salvar(Conta c) {
        var e = new ContaJpa();
        e.id = c.getId(); e.codigo = c.getCodigo().valor(); e.nome = c.getNome(); e.tipo = c.getTipo().name();
        e.natureza = c.getNatureza().name(); e.contaPaiId = c.getContaPaiId(); e.ativa = c.isAtiva();
        e.criadoEm = c.getCriadoEm(); e.atualizadoEm = c.getAtualizadoEm();
        return toDomain(jpa.save(e));
    }

    @Override public Optional<Conta> buscarPorId(UUID id) { return jpa.findById(id).map(this::toDomain); }

    @Override public Optional<Conta> buscarPorCodigo(CodigoConta codigo) { return jpa.findByCodigo(codigo.valor()).map(this::toDomain); }

    @Override public List<Conta> buscarPorIds(Collection<UUID> ids) { return jpa.findAllById(ids).stream().map(this::toDomain).toList(); }

    @Override
    public List<Conta> listar(TipoConta tipo, NaturezaSaldo natureza) {
        return jpa.findAll(Sort.by("codigo")).stream()
                .filter(e -> tipo == null || e.tipo.equals(tipo.name()))
                .filter(e -> natureza == null || e.natureza.equals(natureza.name()))
                .map(this::toDomain).toList();
    }

    @Override public boolean possuiFilhas(UUID contaId) { return jpa.existsByContaPaiId(contaId); }

    private Conta toDomain(ContaJpa e) {
        return Conta.reconstituir(e.id, new CodigoConta(e.codigo), e.nome, TipoConta.valueOf(e.tipo),
                NaturezaSaldo.valueOf(e.natureza), e.contaPaiId, e.ativa, e.criadoEm, e.atualizadoEm);
    }
}
