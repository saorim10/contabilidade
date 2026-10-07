package br.com.contabilidade.domain.model;

import br.com.contabilidade.domain.enums.NaturezaSaldo;
import br.com.contabilidade.domain.enums.TipoConta;
import br.com.contabilidade.domain.event.ContaInativada;
import br.com.contabilidade.domain.exception.RegraNegocioException;
import br.com.contabilidade.domain.vo.CodigoConta;
import br.com.contabilidade.domain.vo.Dinheiro;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Conta extends AggregateRoot {
    private final UUID id;
    private final CodigoConta codigo;
    private final String nome;
    private final TipoConta tipo;
    private final NaturezaSaldo natureza;
    private final UUID contaPaiId;
    private boolean ativa;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Conta(UUID id, CodigoConta codigo, String nome, TipoConta tipo, NaturezaSaldo natureza, UUID contaPaiId,
                  boolean ativa, Instant criadoEm, Instant atualizadoEm) {
        this.id = id; this.codigo = codigo; this.nome = nome; this.tipo = tipo; this.natureza = natureza;
        this.contaPaiId = contaPaiId; this.ativa = ativa; this.criadoEm = criadoEm; this.atualizadoEm = atualizadoEm;
    }

    public static Conta criar(CodigoConta codigo, String nome, TipoConta tipo, NaturezaSaldo natureza, UUID contaPaiId) {
        Objects.requireNonNull(codigo, "codigo");
        Objects.requireNonNull(tipo, "tipo");
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome da conta é obrigatório");
        if (nome.trim().length() > 120) throw new IllegalArgumentException("Nome da conta excede 120 caracteres");
        var agora = Instant.now();
        return new Conta(UUID.randomUUID(), codigo, nome.trim(), tipo, natureza != null ? natureza : tipo.naturezaPadrao(),
                contaPaiId, true, agora, agora);
    }

    public static Conta reconstituir(UUID id, CodigoConta codigo, String nome, TipoConta tipo, NaturezaSaldo natureza,
                                     UUID contaPaiId, boolean ativa, Instant criadoEm, Instant atualizadoEm) {
        return new Conta(id, codigo, nome, tipo, natureza, contaPaiId, ativa, criadoEm, atualizadoEm);
    }

    /** RN-08: só inativa com saldo zero. */
    public void inativar(Dinheiro saldo, UUID usuarioId) {
        if (!ativa) throw new RegraNegocioException("CONTA-03", "Conta " + codigo + " já está inativa");
        if (!saldo.isZero()) {
            throw new RegraNegocioException("RN-08", "Conta só pode ser inativada com saldo zero (saldo atual: " + saldo.valor() + ")");
        }
        ativa = false;
        atualizadoEm = Instant.now();
        registrar(new ContaInativada(id, codigo.valor(), usuarioId, atualizadoEm));
    }

    /** RN-04. A restrição de conta sintética (RN-09) depende da hierarquia e é validada no caso de uso. */
    public boolean podeReceberLancamento() { return ativa; }

    public UUID getId() { return id; }
    public CodigoConta getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public TipoConta getTipo() { return tipo; }
    public NaturezaSaldo getNatureza() { return natureza; }
    public UUID getContaPaiId() { return contaPaiId; }
    public boolean isAtiva() { return ativa; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
