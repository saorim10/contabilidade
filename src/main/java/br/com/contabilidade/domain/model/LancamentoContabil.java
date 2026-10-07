package br.com.contabilidade.domain.model;

import br.com.contabilidade.domain.enums.OrigemLancamento;
import br.com.contabilidade.domain.enums.StatusLancamento;
import br.com.contabilidade.domain.enums.TipoPartida;
import br.com.contabilidade.domain.event.LancamentoCriado;
import br.com.contabilidade.domain.event.LancamentoEstornado;
import br.com.contabilidade.domain.exception.RegraNegocioException;
import br.com.contabilidade.domain.vo.Competencia;
import br.com.contabilidade.domain.vo.Dinheiro;
import br.com.contabilidade.domain.vo.NumeroLancamento;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Raiz de agregado. Imutável após confirmado (RN-13): a única mudança permitida é CONFIRMADO -> ESTORNADO. */
public class LancamentoContabil extends AggregateRoot {
    private final UUID id;
    private final NumeroLancamento numero;
    private final LocalDate data;
    private final String historico;
    private StatusLancamento status;
    private final OrigemLancamento origem;
    private final List<Partida> partidas;
    private final UUID criadoPor;
    private final Instant criadoEm;
    private final UUID estornoDeId;

    private LancamentoContabil(UUID id, NumeroLancamento numero, LocalDate data, String historico, StatusLancamento status,
                               OrigemLancamento origem, List<Partida> partidas, UUID criadoPor, Instant criadoEm, UUID estornoDeId) {
        this.id = id; this.numero = numero; this.data = data; this.historico = historico; this.status = status;
        this.origem = origem; this.partidas = new ArrayList<>(partidas); this.criadoPor = criadoPor;
        this.criadoEm = criadoEm; this.estornoDeId = estornoDeId;
    }

    /** Cria um lançamento em RASCUNHO. O balanceamento é exigido na confirmação. */
    public static LancamentoContabil criar(NumeroLancamento numero, LocalDate data, String historico, OrigemLancamento origem,
                                           List<Partida> partidas, UUID criadoPor) {
        Objects.requireNonNull(numero, "numero");
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(origem, "origem");
        Objects.requireNonNull(partidas, "partidas");
        Objects.requireNonNull(criadoPor, "criadoPor");
        if (historico == null || historico.isBlank()) throw new IllegalArgumentException("Histórico é obrigatório");
        if (!numero.competencia().equals(Competencia.of(data))) {
            throw new IllegalArgumentException("Número do lançamento não corresponde à competência da data");
        }
        return new LancamentoContabil(UUID.randomUUID(), numero, data, historico.trim(), StatusLancamento.RASCUNHO, origem,
                partidas, criadoPor, Instant.now(), null);
    }

    public static LancamentoContabil reconstituir(UUID id, NumeroLancamento numero, LocalDate data, String historico,
                                                  StatusLancamento status, OrigemLancamento origem, List<Partida> partidas,
                                                  UUID criadoPor, Instant criadoEm, UUID estornoDeId) {
        return new LancamentoContabil(id, numero, data, historico, status, origem, partidas, criadoPor, criadoEm, estornoDeId);
    }

    public void adicionarPartida(Partida partida) {
        if (status != StatusLancamento.RASCUNHO) {
            throw new RegraNegocioException("RN-13", "Lançamentos são imutáveis; correção só via estorno");
        }
        partidas.add(partida);
    }

    /** RN-01 e RN-02. */
    public void validarBalanceamento() {
        if (partidas.size() < 2) throw new RegraNegocioException("RN-02", "Lançamento precisa ter no mínimo 2 partidas");
        var debitos = totalDebitos();
        var creditos = totalCreditos();
        if (debitos.valor().compareTo(creditos.valor()) != 0) {
            throw new RegraNegocioException("RN-01", "Partidas dobradas: soma dos débitos (%s) difere da soma dos créditos (%s)"
                    .formatted(debitos.valor(), creditos.valor()));
        }
    }

    public void confirmar() {
        if (status != StatusLancamento.RASCUNHO) {
            throw new RegraNegocioException("LANC-01", "Somente rascunhos podem ser confirmados (status atual: " + status + ")");
        }
        validarBalanceamento();
        status = StatusLancamento.CONFIRMADO;
        registrar(new LancamentoCriado(id, numero.toString(), origem, totalDebitos().valor(), criadoPor, Instant.now()));
    }

    /** RN-07: não apaga o original; gera um lançamento com partidas invertidas e marca o original como ESTORNADO. */
    public LancamentoContabil estornar(String motivo, NumeroLancamento numeroEstorno, LocalDate dataEstorno, UUID usuarioId) {
        if (motivo == null || motivo.isBlank()) throw new RegraNegocioException("RN-07", "Motivo do estorno é obrigatório");
        if (status != StatusLancamento.CONFIRMADO) {
            throw new RegraNegocioException("RN-07", "Somente lançamentos confirmados podem ser estornados (status atual: " + status + ")");
        }
        if (origem == OrigemLancamento.ESTORNO) {
            throw new RegraNegocioException("RN-07", "Um lançamento de estorno não pode ser estornado");
        }
        var invertidas = partidas.stream().map(Partida::inversa).toList();
        var estorno = new LancamentoContabil(UUID.randomUUID(), numeroEstorno, dataEstorno,
                "Estorno do lançamento " + numero + ": " + motivo.trim(), StatusLancamento.RASCUNHO,
                OrigemLancamento.ESTORNO, invertidas, usuarioId, Instant.now(), this.id);
        estorno.confirmar();
        this.status = StatusLancamento.ESTORNADO;
        registrar(new LancamentoEstornado(id, numero.toString(), estorno.id, estorno.numero.toString(), motivo.trim(), usuarioId, Instant.now()));
        return estorno;
    }

    public Dinheiro totalDebitos() { return soma(TipoPartida.DEBITO); }

    public Dinheiro totalCreditos() { return soma(TipoPartida.CREDITO); }

    private Dinheiro soma(TipoPartida tipo) {
        return partidas.stream().filter(p -> p.tipo() == tipo).map(Partida::valor).reduce(Dinheiro.zero(), Dinheiro::somar);
    }

    public Competencia getCompetencia() { return Competencia.of(data); }
    public UUID getId() { return id; }
    public NumeroLancamento getNumero() { return numero; }
    public LocalDate getData() { return data; }
    public String getHistorico() { return historico; }
    public StatusLancamento getStatus() { return status; }
    public OrigemLancamento getOrigem() { return origem; }
    public List<Partida> getPartidas() { return Collections.unmodifiableList(partidas); }
    public UUID getCriadoPor() { return criadoPor; }
    public Instant getCriadoEm() { return criadoEm; }
    public UUID getEstornoDeId() { return estornoDeId; }
}
