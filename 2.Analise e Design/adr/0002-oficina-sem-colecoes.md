# ADR-0002: Oficina não mantém coleções dos registros que a referenciam

Status: Aceita
Data: 2026-09-18
Relacionado: ADR-0001 (single-tenant)

## Contexto

A `Oficina` se relaciona com `Cliente`, `Usuario` e `OrdemServico`, sempre
na proporção uma oficina para zero ou mais registros (`0..*`).

A primeira versão do diagrama modelava essas relações a partir da `Oficina`
(navegabilidade `Oficina → X`, com listas `clientes`, `mecanicos` e
`ordensServicos`) e usava losangos de agregação/composição.

Ao discutir a implementação, concluímos que:

- Cliente, usuário e ordem de serviço existem independentemente da oficina e
  sobrevivem a ela. Não há relação todo-parte.
- Clientes e ordens de serviço crescem sem limite. Uma lista na entidade
  carrega tudo de uma vez, sem paginação, filtro ou ordenação no banco.
- Coleções no lado "um" trazem `LazyInitializationException`, N+1 e risco de
  loops de serialização, além de exigir sincronizar os dois lados da relação.
- Com listas, o pacote `oficina` passaria a importar `cliente`, `usuario` e
  `ordemservico`, que já dependem de `oficina` (ciclo entre pacotes).

## Decisão

1. **Navegabilidade unidirecional, sem listas na `Oficina`:**
   - `Cliente → Oficina`
   - `Usuario → Oficina`
   - `OrdemServico → Oficina`
2. **Associações simples**, sem agregação nem composição. Composição fica
   reservada às partes que morrem com o todo (`OrdemServico` ◆ `ItemPeca` e
   `OrdemServico` ◆ `ItemServico`).
3. **Multiplicidades** (lado da `Oficina`; o outro lado é sempre `0..*`):
   - `Cliente → Oficina`: `1`
   - `OrdemServico → Oficina`: `1`
   - `Usuario → Oficina`: `0..1` ou `1` (ver "Pontos em aberto")
4. **Implementação:** `@ManyToOne(fetch = FetchType.LAZY)` no lado "muitos",
   com `@JoinColumn(name = "oficina_id")`, sem `cascade`.
5. **Consultas** "registros da oficina" ficam no service do domínio dono do
   dado, com paginação, por exemplo `ClienteService.listarPorOficina(...)`
   e `findByOficinaId(oficinaId, pageable)` no repository. A rota pode ser
   `GET /clientes?oficinaId=1`.

## Consequências

Positivas:

- Consultas paginadas, o que a API precisa devolver aos frontends
  (React, JSF e Android).
- Sem N+1 nem `LazyInitializationException` por causa dessas listas.
- Dependência em uma direção entre pacotes: `cliente`, `usuario` e
  `ordemservico` dependem de `oficina`, nunca o contrário.
- A `Oficina` não tem cascade sobre históricos (apagar uma oficina não apaga
  ordens de serviço).

Custos:

- Não existe `oficina.getClientes()`. Quem precisar listar os registros de uma
  oficina faz uma consulta explícita pelo service do domínio correspondente.

## Alternativas consideradas

- **Bidirecional (`mappedBy` + `LAZY`) para clientes e ordens:** descartada
  pelos motivos do contexto (crescimento sem limite, ciclo entre pacotes,
  sincronização dos dois lados, risco de serialização).
- **`@OneToMany` sem `mappedBy`:** descartada, pois o Hibernate criaria tabelas
  de junção em vez da coluna `oficina_id`.
- **Agregação ou composição a partir da `Oficina`:** descartada, pois os
  registros não são partes da oficina.

## Exceção prevista

Se surgir um requisito de navegar de `Oficina` para uma coleção pequena,
limitada e sempre usada junto com ela, a decisão pode ser revista para aquele
caso específico (com `mappedBy`, `LAZY` e sem `cascade`), em um novo ADR.

## Pontos em aberto

- **`Usuario → Oficina`:** `0..1` (perfis como SUPERADMIN podem não pertencer a
  uma filial) ou `1` (todo usuário pertence a uma oficina).
- **Ciclo de vida da `Oficina`:** exclusão física ou desativação (campo
  `ativo`). Desativar preserva o histórico.
- **Oficina na `OrdemServico`:** guardar `oficina_id` na própria ordem
  (recomendado, pois o mecânico pode ser transferido de filial sem alterar
  ordens antigas) ou derivá-la do mecânico responsável.

## Ajustes no diagrama

- Inverter a seta das três ligações para que termine em `Oficina`.
- Remover os losangos dessas ligações.
- Remover os papéis `clientes`, `mecanicos` e `ordensServicos`; usar `oficina`
  na ponta da `Oficina`.
- Manter uma nota UML ligada às classes afetadas: "Ver ADR-0002".