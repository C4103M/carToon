-- =============================================================================
-- CART - Massa de Testes | Script 02 - Carga da massa de testes
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- 1. OFICINAS
-- -----------------------------------------------------------------------------

INSERT INTO oficina (id, nome, endereco, telefone, email, valor_hora, taxa_risco, margem_peca, ativo) VALUES
 (1, 'CarToon Centro', 'Rua XV de Novembro, 500 - Centro, Curitiba - PR',     '(41) 3333-1001', 'centro@cartoon.com.br', 120.00, 0.1000, 1.2000, TRUE),
 (2, 'CarToon Sul',    'Av. Winston Churchill, 1200 - Pinheirinho, Curitiba - PR', '(41) 3333-2002', 'sul@cartoon.com.br',    100.00, 0.0500, 1.1500, TRUE),
 (3, 'CarToon Norte',  'Rua Mateus Leme, 3000 - São Lourenço, Curitiba - PR', '(41) 3333-3003', 'norte@cartoon.com.br',   90.00, 0.0800, 1.1000, FALSE),
 (4, 'CarToon Leste',  'Av. Senador Salgado Filho, 800 - Guabirotuba, Curitiba - PR', '(41) 3333-4004', 'leste@cartoon.com.br', 110.00, 0.0500, 1.1000, TRUE);

-- -----------------------------------------------------------------------------
-- 2. USUÁRIOS (senha de todos: Teste@123)
-- -----------------------------------------------------------------------------
INSERT INTO usuario (id, nome, email, senha, role, ativo, oficina_id) VALUES
 ( 1, 'Super Administrador',  'superadmin@cartoon.com.br',       '$2a$10$mF5yO2b0YZ2KIim4ATGsEOpzitFpAH.9rbbaETzGPoZwNGyhZZXo.', 'SUPERADMIN', TRUE,  NULL),
 ( 2, 'Carlos Mendes',        'admin.centro@cartoon.com.br',     '$2a$10$mF5yO2b0YZ2KIim4ATGsEOpzitFpAH.9rbbaETzGPoZwNGyhZZXo.', 'ADMIN',      TRUE,  1),
 ( 3, 'Ana Ribeiro',          'admin.sul@cartoon.com.br',        '$2a$10$mF5yO2b0YZ2KIim4ATGsEOpzitFpAH.9rbbaETzGPoZwNGyhZZXo.', 'ADMIN',      TRUE,  2),
 ( 4, 'Bruno Carvalho',       'mecanico.centro@cartoon.com.br',  '$2a$10$mF5yO2b0YZ2KIim4ATGsEOpzitFpAH.9rbbaETzGPoZwNGyhZZXo.', 'MECANICO',   TRUE,  1),
 ( 5, 'Diego Souza',          'mecanico2.centro@cartoon.com.br', '$2a$10$mF5yO2b0YZ2KIim4ATGsEOpzitFpAH.9rbbaETzGPoZwNGyhZZXo.', 'MECANICO',   TRUE,  1),
 ( 6, 'Elisa Martins',        'mecanico.sul@cartoon.com.br',     '$2a$10$mF5yO2b0YZ2KIim4ATGsEOpzitFpAH.9rbbaETzGPoZwNGyhZZXo.', 'MECANICO',   TRUE,  2),
 ( 7, 'Fábio Teixeira',       'admin.norte@cartoon.com.br',      '$2a$10$mF5yO2b0YZ2KIim4ATGsEOpzitFpAH.9rbbaETzGPoZwNGyhZZXo.', 'ADMIN',      TRUE,  3),
 ( 8, 'Helena Prado',         'mecanico.inativo@cartoon.com.br', '$2a$10$mF5yO2b0YZ2KIim4ATGsEOpzitFpAH.9rbbaETzGPoZwNGyhZZXo.', 'MECANICO',   FALSE, 1),
 ( 9, 'Igor Lopes',           'mecanico.bloqueio@cartoon.com.br','$2a$10$mF5yO2b0YZ2KIim4ATGsEOpzitFpAH.9rbbaETzGPoZwNGyhZZXo.', 'MECANICO',   TRUE,  1),
 (10, 'Juliana Rocha',        'admin.leste@cartoon.com.br',      '$2a$10$mF5yO2b0YZ2KIim4ATGsEOpzitFpAH.9rbbaETzGPoZwNGyhZZXo.', 'ADMIN',      TRUE,  4);

-- -----------------------------------------------------------------------------
-- 3. CLIENTES (CPFs com dígitos verificadores válidos)
-- -----------------------------------------------------------------------------
INSERT INTO cliente (id, nome, cpf, endereco, telefone, ativo, oficina_id) VALUES
 (1, 'João da Silva',     '52998224725', 'Rua das Flores, 120 - Batel, Curitiba - PR',        '(41) 99999-1001', TRUE,  1),
 (2, 'Maria Oliveira',    '11144477735', 'Av. Sete de Setembro, 2500 - Centro, Curitiba - PR', '(41) 99999-1002', TRUE,  1),
 (3, 'Pedro Santos',      '39053344705', 'Rua Chile, 45 - Rebouças, Curitiba - PR',            '(41) 99999-1003', TRUE,  1),
 (4, 'Roberto Almeida',   '27618365024', 'Rua Itupava, 900 - Alto da XV, Curitiba - PR',       '(41) 99999-1004', FALSE, 1),
 (5, 'Luciana Ferreira',  '98765432100', 'Rua Carlos Dietzsch, 300 - Portão, Curitiba - PR',   '(41) 99999-2001', TRUE,  2);

-- -----------------------------------------------------------------------------
-- 4. VEÍCULOS
-- -----------------------------------------------------------------------------
-- 1 histórico e paginação
-- 2 "sem histórico" e inativação
-- 3 bloqueio de inativação
-- 4 erro de cálculo do serviço
-- 5 isolamento entre oficinas
-- 6 reativação / OS bloqueada
INSERT INTO veiculo (id, placa, marca, modelo, ano_modelo, combustivel, codigo_fipe, valor_fipe, mes_referencia, ativo, cliente_id) VALUES
 (1, 'ABC1D23',  'Fiat',       'Strada Freedom 1.3 Flex',   2022, 'Flex',     '001501-6',  98000.00, 'novembro de 2026', TRUE,  1),
 (2, 'BRA2E19',  'Volkswagen', 'Gol 1.0 MPI Flex',          2015, 'Flex',     '005340-6',  32000.00, 'novembro de 2026', TRUE,  1),
 (3, 'QWE-4567', 'Honda',      'Civic Sedan EXL 2.0 Flex',  2020, 'Flex',     '014079-1', 115000.00, 'novembro de 2026', TRUE,  2),
 (4, 'RTY8U90',  'Toyota',     'Corolla GLi 1.8 Flex',      2018, 'Flex',     NULL,            NULL,  NULL,               TRUE,  2),
 (5, 'JKL3M45',  'Renault',    'Kwid Zen 1.0 Flex',         2021, 'Flex',     '025271-9',  45000.00, 'novembro de 2026', TRUE,  5),
 (6, 'MNB-1234', 'Chevrolet',  'Onix 1.0 LT',               2017, 'Flex',     '004403-2',  52000.00, 'novembro de 2026', FALSE, 1);

-- -----------------------------------------------------------------------------
-- 5. CATÁLOGO DE PEÇAS
-- -----------------------------------------------------------------------------
INSERT INTO peca (id, nome, fabricante, valor_base, ativo, oficina_id) VALUES
 (1, 'Filtro de Óleo',                    'Tecfil', 35.00,  TRUE,  1),
 (2, 'Óleo de Motor 5W30 Sintético 1L',   'Mobil',  48.00,  TRUE,  1),
 (3, 'Jogo de Pastilhas de Freio Dianteiro', 'Cobreq', 180.00, TRUE,  1),
 (4, 'Jogo de Velas de Ignição Iridium',  'NGK',    260.00, TRUE,  1),  -- sem uso em OS
 (5, 'Lâmpada H4 Farol',                  'Philips', 42.00, FALSE, 1),  -- INATIVA
 (6, 'Filtro de Ar do Motor',             'Mann',   55.00,  TRUE,  2);

-- -----------------------------------------------------------------------------
-- 6. CATÁLOGO DE SERVIÇOS
-- -----------------------------------------------------------------------------
INSERT INTO servico (id, nome, tempo, descricao, peso_servico, ativo, oficina_id) VALUES
 (1, 'Troca de Óleo e Filtro',             '00:45:00', 'Drenagem do óleo, troca do filtro e reabastecimento.',          0.0, TRUE,  1),
 (2, 'Revisão do Sistema de Freios',       '02:00:00', 'Inspeção de discos e pastilhas, sangria e troca de fluido.',    0.5, TRUE,  1),
 (3, 'Substituição da Correia Dentada',    '04:00:00', 'Troca do kit de distribuição e sincronismo do motor.',          1.0, TRUE,  1),
 (4, 'Diagnóstico com Scanner OBD-II',     '01:00:00', 'Leitura de falhas das centrais eletrônicas.',                   0.0, TRUE,  1),  -- sem uso em OS
 (5, 'Polimento Técnico',                  '03:00:00', 'Polimento e cristalização da pintura.',                         0.0, FALSE, 1),  -- INATIVO
 (6, 'Alinhamento e Balanceamento',        '01:00:00', 'Alinhamento da suspensão e balanceamento das 4 rodas.',         0.0, TRUE,  2);

-- -----------------------------------------------------------------------------
-- 7. ORDENS DE SERVIÇO
-- -----------------------------------------------------------------------------
INSERT INTO ordem_servico (id, versao, descricao, data_orcamento, data_rejeicao, data_inicio, data_finalizacao,
                           desconto, valor_bruto, valor_desconto, total, status_servico, oficina_id, mecanico_id, veiculo_id) VALUES
 (1, 0, 'Troca de óleo e filtro - revisão 30.000 km',      '2026-08-10', NULL,         '2026-08-11', '2026-08-12', 'CINCO_PORCENTO', 362.40, 18.12, 344.28, 'FINALIZADO',   1, 4, 1),
 (2, 0, 'Barulho ao frear - revisão dos freios',           '2026-09-15', '2026-09-16', NULL,         NULL,         'SEM_DESCONTO',   467.76,  0.00, 467.76, 'REJEITADO',    1, 4, 1),
 (3, 0, 'Troca preventiva da correia dentada',             '2026-11-20', NULL,         NULL,         NULL,         'SEM_DESCONTO',   527.04,  0.00, 527.04, 'PENDENTE',     1, 4, 1),
 (4, 0, 'Pedal de freio baixo',                            '2026-11-18', NULL,         '2026-11-19', NULL,         'DEZ_PORCENTO',   469.80, 46.98, 422.82, 'EM_ANDAMENTO', 1, 4, 3),
 (5, 0, 'Troca de óleo programada',                        '2026-11-21', NULL,         NULL,         NULL,         'SEM_DESCONTO',   132.00,  0.00, 132.00, 'ACEITO',       1, 4, 3),
 (6, 0, 'Alinhamento e troca do filtro de ar',             '2026-11-22', NULL,         NULL,         NULL,         'SEM_DESCONTO',   163.25,  0.00, 163.25, 'PENDENTE',     2, 6, 5),
 (7, 0, 'Avaliação de ruído na suspensão (sem itens)',     '2026-11-19', NULL,         '2026-11-20', NULL,         'SEM_DESCONTO',     0.00,  0.00,   0.00, 'EM_ANDAMENTO', 1, 4, 3);

INSERT INTO ordem_servico (id, versao, descricao, data_orcamento, data_inicio, data_finalizacao,
                           desconto, valor_bruto, valor_desconto, total, status_servico, oficina_id, mecanico_id, veiculo_id)
SELECT n,
       0,
       'Troca de óleo e filtro - manutenção periódica',
       make_date(2025, n - 100, 10),
       make_date(2025, n - 100, 10),
       make_date(2025, n - 100, 11),
       'SEM_DESCONTO', 90.00, 0.00, 90.00, 'FINALIZADO', 1, 4, 1
FROM generate_series(101, 110) AS n;

-- -----------------------------------------------------------------------------
-- 8. ITENS DE SERVIÇO
-- -----------------------------------------------------------------------------
-- valor_unitario = horas x valor_hora x (1 + fipe/100000 x taxa_risco x peso)
INSERT INTO item_servico (id, quantidade, valor_unitario, subtotal, servico_id, tempo, peso_servico, ordem_servico_id) VALUES
 (1, 1,  90.00,  90.00, 1, '00:45:00', 0.00, 1),
 (2, 1, 251.76, 251.76, 2, '02:00:00', 0.50, 2),
 (3, 1, 527.04, 527.04, 3, '04:00:00', 1.00, 3),
 (4, 1, 253.80, 253.80, 2, '02:00:00', 0.50, 4),
 (5, 1,  90.00,  90.00, 1, '00:45:00', 0.00, 5),
 (6, 1, 100.00, 100.00, 6, '01:00:00', 0.00, 6);

-- Histórico antigo
INSERT INTO item_servico (id, quantidade, valor_unitario, subtotal, servico_id, tempo, peso_servico, ordem_servico_id)
SELECT n - 94, 1, 90.00, 90.00, 1, '00:45:00', 0.00, n
FROM generate_series(101, 110) AS n;

-- -----------------------------------------------------------------------------
-- 9. ITENS DE PEÇA
-- -----------------------------------------------------------------------------
-- valor_unitario = valor_base x margem_peca da oficina (Centro 1,20 | Sul 1,15)
INSERT INTO item_peca (id, quantidade, valor_base, margem, valor_unitario, subtotal, peca_id, ordem_servico_id) VALUES
 (1, 1,  35.00, 1.2000,  42.00,  42.00, 1, 1),
 (2, 4,  48.00, 1.2000,  57.60, 230.40, 2, 1),
 (3, 1, 180.00, 1.2000, 216.00, 216.00, 3, 2),
 (4, 1, 180.00, 1.2000, 216.00, 216.00, 3, 4),
 (5, 1,  35.00, 1.2000,  42.00,  42.00, 1, 5),
 (6, 1,  55.00, 1.1500,  63.25,  63.25, 6, 6);

-- -----------------------------------------------------------------------------
-- 10. AJUSTE DAS SEQUÊNCIAS (próximos cadastros feitos pela tela não colidem)
-- -----------------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('oficina',       'id'), (SELECT MAX(id) FROM oficina));
SELECT setval(pg_get_serial_sequence('usuario',       'id'), (SELECT MAX(id) FROM usuario));
SELECT setval(pg_get_serial_sequence('cliente',       'id'), (SELECT MAX(id) FROM cliente));
SELECT setval(pg_get_serial_sequence('veiculo',       'id'), (SELECT MAX(id) FROM veiculo));
SELECT setval(pg_get_serial_sequence('peca',          'id'), (SELECT MAX(id) FROM peca));
SELECT setval(pg_get_serial_sequence('servico',       'id'), (SELECT MAX(id) FROM servico));
SELECT setval(pg_get_serial_sequence('ordem_servico', 'id'), (SELECT MAX(id) FROM ordem_servico));
SELECT setval(pg_get_serial_sequence('item_servico',  'id'), (SELECT MAX(id) FROM item_servico));
SELECT setval(pg_get_serial_sequence('item_peca',     'id'), (SELECT MAX(id) FROM item_peca));

COMMIT;
