-- =============================================================================
-- CART - Massa de Testes | Script 01 - Limpeza da base
-- =============================================================================

BEGIN;

TRUNCATE TABLE
    item_servico,
    item_peca,
    ordem_servico,
    veiculo,
    cliente,
    peca,
    servico,
    usuario,
    oficina
RESTART IDENTITY CASCADE;

COMMIT;
