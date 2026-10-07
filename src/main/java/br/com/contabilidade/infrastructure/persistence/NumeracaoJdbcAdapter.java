package br.com.contabilidade.infrastructure.persistence;

import br.com.contabilidade.domain.repository.GeradorNumeroLancamento;
import br.com.contabilidade.domain.vo.Competencia;
import br.com.contabilidade.domain.vo.NumeroLancamento;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** RN-14: sequência por competência via UPSERT atômico; participa da transação (sem lacunas em caso de rollback). */
@Component
public class NumeracaoJdbcAdapter implements GeradorNumeroLancamento {
    private final JdbcTemplate jdbc;

    public NumeracaoJdbcAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public NumeroLancamento proximo(Competencia competencia) {
        Integer seq = jdbc.queryForObject("""
                INSERT INTO lancamento_sequencia (competencia, ultimo) VALUES (?, 1)
                ON CONFLICT (competencia) DO UPDATE SET ultimo = lancamento_sequencia.ultimo + 1
                RETURNING ultimo
                """, Integer.class, competencia.toString());
        return new NumeroLancamento(competencia, seq);
    }
}
