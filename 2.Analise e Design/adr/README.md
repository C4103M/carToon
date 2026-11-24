# Registros de Decisão de Arquitetura (ADR)

| ADR | Título | Status |
|---|---|---|
| [0001](0001-single-tenant.md) | Sistema single-tenant (uma empresa, uma oficina) | Substituída pelo ADR-0003 |
| [0002](0002-oficina-sem-colecoes.md) | Oficina não mantém coleções dos registros que a referenciam | Aceita |
| [0003](0003-multi-tenant-tenant-guard.md) | Arquitetura multi-tenant com isolamento lógico por oficina (TenantGuard) | Aceita |
| [0004](0004-jwt-cookie-seguranca.md) | Sessão stateless via JWT em cookie HttpOnly, com revogação ativa e proteção contra força bruta | Aceita |

Observação: um ADR-0003 anterior, sobre filiais, foi retirado da documentação
em 30/09/2026, quando as filiais saíram do escopo. O número 0003 passou a
registrar a decisão multi-tenant.
