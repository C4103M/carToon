# ADR-0001: Sistema single-tenant (uma empresa, várias filiais)

Status: Aceita
Data: 2026-09-18

## Contexto
O sistema atende uma única empresa, que pode ter várias oficinas
(matriz e filiais). Não há necessidade de isolar dados entre oficinas
como se fossem clientes independentes do sistema.

## Decisão
O sistema é single-tenant. Não existe entidade "Empresa/tenant" nem
coluna tenant_id. Oficina é uma unidade da mesma empresa.

## Consequências no modelo
- Cliente–Oficina: associação simples (cliente não pertence a uma filial).
- Usuario–Oficina: associação simples; SUPERADMIN tem escopo global.
- Unicidade global: placa do veículo e CPF do cliente são únicos no sistema.
- Consultas não filtram por tenant.

## Alternativas consideradas
- Multi-tenant desde o início: descartada por adicionar complexidade
  (isolamento, filtros em toda query, gestão de tenants) sem necessidade atual.

## Gatilho para revisar
Se outra empresa independente precisar usar o sistema com dados isolados.

## Se migrarmos para multi-tenant (impacto previsto)
- Modelo: nova entidade Empresa (tenant); Oficina passa a pertencer a ela
  (aqui composição ou agregação Empresa→Oficina passa a fazer sentido).
  Cliente, Veiculo, Usuario e OrdemServico ganham vínculo com a Empresa.
- Unicidade: placa e CPF passam a ser únicos por empresa, não globalmente.
- Perfis: SUPERADMIN passa a ser da plataforma; ADMIN, da empresa.
- Segurança: tenant vai no token e é aplicado em toda consulta
  (filtro no Hibernate ou row-level security no Postgres).
- Banco: migration adicionando a coluna, preenchendo com a empresa atual
  e recriando as constraints de unicidade.
- Diagrama: revisar Oficina, Cliente, Usuario, Veiculo e OrdemServico.