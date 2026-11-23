-- =============================================================================
-- CART - Massa de Testes | Script 03 - Conferência da massa
-- =============================================================================

-- 1. Quantidade de registros por tabela
SELECT tabela, esperado, encontrado,
       CASE WHEN esperado = encontrado THEN 'OK' ELSE 'DIVERGENTE' END AS situacao
FROM (
    SELECT 'oficina'       AS tabela,  4 AS esperado, (SELECT COUNT(*) FROM oficina)       AS encontrado UNION ALL
    SELECT 'usuario',                 10,             (SELECT COUNT(*) FROM usuario)                     UNION ALL
    SELECT 'cliente',                  5,             (SELECT COUNT(*) FROM cliente)                     UNION ALL
    SELECT 'veiculo',                  6,             (SELECT COUNT(*) FROM veiculo)                     UNION ALL
    SELECT 'peca',                     6,             (SELECT COUNT(*) FROM peca)                        UNION ALL
    SELECT 'servico',                  6,             (SELECT COUNT(*) FROM servico)                     UNION ALL
    SELECT 'ordem_servico',           17,             (SELECT COUNT(*) FROM ordem_servico)               UNION ALL
    SELECT 'item_servico',            16,             (SELECT COUNT(*) FROM item_servico)                UNION ALL
    SELECT 'item_peca',                6,             (SELECT COUNT(*) FROM item_peca)
) t
ORDER BY tabela;

-- 2. Valor de cada item de serviço recalculado pela fórmula da API
--    horas x valor_hora x (1 + fipe/100000 x taxa_risco x peso)
SELECT i.id AS item, i.ordem_servico_id AS os, s.nome AS servico,
       i.valor_unitario AS gravado,
       ROUND(EXTRACT(EPOCH FROM i.tempo) / 3600.0 * o.valor_hora
             * (1 + (v.valor_fipe / 100000.0) * o.taxa_risco * i.peso_servico), 2) AS calculado,
       CASE WHEN i.valor_unitario = ROUND(EXTRACT(EPOCH FROM i.tempo) / 3600.0 * o.valor_hora
             * (1 + (v.valor_fipe / 100000.0) * o.taxa_risco * i.peso_servico), 2)
            THEN 'OK' ELSE 'DIVERGENTE' END AS situacao
FROM item_servico i
JOIN servico s        ON s.id = i.servico_id
JOIN ordem_servico os ON os.id = i.ordem_servico_id
JOIN oficina o        ON o.id = os.oficina_id
JOIN veiculo v        ON v.id = os.veiculo_id
ORDER BY i.id;

-- 3. Valor de cada item de peça recalculado (valor_base x margem da oficina)
SELECT i.id AS item, i.ordem_servico_id AS os, p.nome AS peca,
       i.valor_unitario AS gravado,
       ROUND(p.valor_base * o.margem_peca, 2) AS calculado,
       CASE WHEN i.valor_unitario = ROUND(p.valor_base * o.margem_peca, 2)
             AND i.subtotal = i.valor_unitario * i.quantidade
            THEN 'OK' ELSE 'DIVERGENTE' END AS situacao
FROM item_peca i
JOIN peca p           ON p.id = i.peca_id
JOIN ordem_servico os ON os.id = i.ordem_servico_id
JOIN oficina o        ON o.id = os.oficina_id
ORDER BY i.id;

-- 4. Totais de cada OS (bruto = soma dos itens; total = bruto - desconto)
SELECT os.id AS os, os.status_servico, os.desconto,
       os.valor_bruto, os.valor_desconto, os.total,
       COALESCE(ps.soma, 0) + COALESCE(ss.soma, 0) AS bruto_calculado,
       ROUND((COALESCE(ps.soma, 0) + COALESCE(ss.soma, 0)) *
             CASE os.desconto WHEN 'CINCO_PORCENTO' THEN 0.05
                              WHEN 'DEZ_PORCENTO'   THEN 0.10
                              WHEN 'QUINZE_PORCENTO' THEN 0.15
                              ELSE 0 END, 2) AS desconto_calculado,
       CASE WHEN os.valor_bruto = COALESCE(ps.soma, 0) + COALESCE(ss.soma, 0)
             AND os.total = os.valor_bruto - os.valor_desconto
             AND os.valor_desconto = ROUND(os.valor_bruto *
                   CASE os.desconto WHEN 'CINCO_PORCENTO' THEN 0.05
                                    WHEN 'DEZ_PORCENTO'   THEN 0.10
                                    WHEN 'QUINZE_PORCENTO' THEN 0.15
                                    ELSE 0 END, 2)
            THEN 'OK' ELSE 'DIVERGENTE' END AS situacao
FROM ordem_servico os
LEFT JOIN (SELECT ordem_servico_id, SUM(subtotal) AS soma FROM item_peca    GROUP BY ordem_servico_id) ps ON ps.ordem_servico_id = os.id
LEFT JOIN (SELECT ordem_servico_id, SUM(subtotal) AS soma FROM item_servico GROUP BY ordem_servico_id) ss ON ss.ordem_servico_id = os.id
ORDER BY os.id;

-- 5. Situações-chave usadas pelo roteiro
SELECT 'Oficina 3 (Norte) inativa' AS verificacao,
       CASE WHEN (SELECT ativo FROM oficina WHERE id = 3) = FALSE THEN 'OK' ELSE 'DIVERGENTE' END AS situacao
UNION ALL
SELECT 'Oficina 4 (Leste) sem OS em aberto',
       CASE WHEN NOT EXISTS (SELECT 1 FROM ordem_servico WHERE oficina_id = 4) THEN 'OK' ELSE 'DIVERGENTE' END
UNION ALL
SELECT 'Mecânico 5 (Diego) sem OS em aberto',
       CASE WHEN NOT EXISTS (SELECT 1 FROM ordem_servico WHERE mecanico_id = 5
                             AND status_servico IN ('PENDENTE','ACEITO','EM_ANDAMENTO')) THEN 'OK' ELSE 'DIVERGENTE' END
UNION ALL
SELECT 'Veículo BRA2E19 sem histórico',
       CASE WHEN NOT EXISTS (SELECT 1 FROM ordem_servico WHERE veiculo_id = 2) THEN 'OK' ELSE 'DIVERGENTE' END
UNION ALL
SELECT 'Veículo ABC1D23 com 13 OS no histórico',
       CASE WHEN (SELECT COUNT(*) FROM ordem_servico WHERE veiculo_id = 1) = 13 THEN 'OK' ELSE 'DIVERGENTE' END
UNION ALL
SELECT 'Veículo RTY8U90 sem valor FIPE',
       CASE WHEN (SELECT valor_fipe FROM veiculo WHERE id = 4) IS NULL THEN 'OK' ELSE 'DIVERGENTE' END
UNION ALL
SELECT 'Cliente 3 (Pedro) sem veículos',
       CASE WHEN NOT EXISTS (SELECT 1 FROM veiculo WHERE cliente_id = 3) THEN 'OK' ELSE 'DIVERGENTE' END
UNION ALL
SELECT 'Peça 4 e serviço 4 sem uso em OS',
       CASE WHEN NOT EXISTS (SELECT 1 FROM item_peca WHERE peca_id = 4)
             AND NOT EXISTS (SELECT 1 FROM item_servico WHERE servico_id = 4) THEN 'OK' ELSE 'DIVERGENTE' END;
