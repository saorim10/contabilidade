package br.com.contabilidade.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LancamentoJpaRepository extends JpaRepository<LancamentoJpa, UUID> {

    boolean existsByCompetenciaAndStatus(String competencia, String status);

    /** [conta_id, débitos, créditos] por conta, ignorando rascunhos. */
    @Query(value = """
            SELECT p.conta_id,
                   COALESCE(SUM(CASE WHEN p.tipo = 'DEBITO'  THEN p.valor ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.tipo = 'CREDITO' THEN p.valor ELSE 0 END), 0)
              FROM partida p
              JOIN lancamento l ON l.id = p.lancamento_id
             WHERE l.status <> 'RASCUNHO' AND l.data BETWEEN :inicio AND :fim
             GROUP BY p.conta_id
            """, nativeQuery = true)
    List<Object[]> movimentos(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query(value = """
            SELECT COALESCE(SUM(CASE WHEN p.tipo = 'DEBITO'  THEN p.valor ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.tipo = 'CREDITO' THEN p.valor ELSE 0 END), 0)
              FROM partida p
              JOIN lancamento l ON l.id = p.lancamento_id
             WHERE p.conta_id = :contaId AND l.status <> 'RASCUNHO' AND l.data <= :ate
            """, nativeQuery = true)
    List<Object[]> movimentoDaConta(@Param("contaId") UUID contaId, @Param("ate") LocalDate ate);

    /** [data, numero, historico, tipo, valor] */
    @Query("""
            select l.data, l.numero, l.historico, p.tipo, p.valor
              from PartidaJpa p join p.lancamento l
             where p.contaId = :contaId and l.status <> 'RASCUNHO' and l.data between :inicio and :fim
             order by l.data, l.numero, p.id
            """)
    List<Object[]> razao(@Param("contaId") UUID contaId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
