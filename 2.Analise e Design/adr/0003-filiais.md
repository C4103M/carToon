# ADR-0003: Filiais como Oficina ligada à matriz

Status: Proposta
Data: 2026-09-29
Relacionado: ADR-0001 (single-tenant)

## Contexto

A empresa tem uma matriz e filiais. O `SUPERADMIN` cadastra as oficinas
(matriz e filiais) e cada filial é gerenciada por um `ADMIN`. Antes, a
`Oficina` tinha apenas `matriz : boolean`, sem ligar a filial à sua matriz.

## Decisão

1. Filial **não é uma entidade nova**: é uma `Oficina` que aponta para a sua
   matriz.
2. Remover `matriz : boolean` e criar uma associação `Oficina → Oficina`, papel
   `oficinaMatriz`, multiplicidade `0..1` no lado da matriz e `0..*` no lado
   da filial. Sem losango.
3. Ao cadastrar a oficina, o `SUPERADMIN` define:
   - matriz: `oficinaMatriz` vazio (`null`);
   - filial: `oficinaMatriz` recebe o id da matriz.
   "É matriz?" é derivado: `isMatriz()` devolve `oficinaMatriz == null`.
4. `Oficina.ativo : boolean` indica se a oficina está disponível ou não.
5. **Regras validadas no service:**
   - `oficinaMatriz` só pode apontar para uma oficina que também tenha
     `oficinaMatriz` vazio (sem hierarquia de mais de um nível).
   - Uma oficina não pode apontar para si mesma.
   - Oficina inativa não recebe novos clientes, usuários nem ordens.
6. **Escopo de acesso:**
   - `SUPERADMIN`: global. Cria e edita oficinas e filiais (UC02) e define o
     `ADMIN` de cada uma.
   - `ADMIN`: restrito à própria oficina. Gerencia os mecânicos dela (UC03).
   - `MECANICO`: restrito à própria oficina.

## Consequências

- Uma única fonte da verdade: não existe estado inconsistente entre "é matriz"
  e "tem matriz".
- Listar matrizes: `findByOficinaMatrizIsNull()`. Listar filiais:
  `findByOficinaMatrizId(id, pageable)`. Sem lista `filiais` na entidade.
- A matriz não tem poder especial sobre as filiais; a ligação serve para
  organizar a rede e consultar.
- Relatório consolidado da matriz não está no escopo; se virar requisito,
  exige revisar o escopo do `ADMIN`.

## Banco de dados

```sql
oficina_matriz_id INTEGER REFERENCES oficina(id),
CONSTRAINT ck_oficina_nao_propria CHECK (oficina_matriz_id <> id)
```

## Alternativas consideradas

- **Manter `matriz : boolean` junto com a associação:** descartada, duplica a
  informação e permite estado inconsistente.
- **Entidade `Filial` separada:** descartada, duplicaria a `Oficina`.
- **`ADMIN` da matriz com visão de todas as filiais:** descartada por ora.

## Ajustes no diagrama

- Remover `matriz : boolean` da `Oficina` (mantém `ativo`).
- Ligação `Oficina → Oficina`, papel `oficinaMatriz`: `0..1` na matriz e
  `0..*` na filial.
- UC02 "Manter Oficinas" passa a cobrir filiais e a definição do `ADMIN`.
