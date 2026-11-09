# ADR-0003: Arquitetura multi-tenant com isolamento lógico por oficina (TenantGuard)

Status: Aceita
Data: 2026-10-26
Substitui: ADR-0001 (single-tenant)
Relacionado: ADR-0002 (Oficina sem coleções), ADR-0004 (sessão via JWT em cookie)

## Contexto

O ADR-0001 previa uma única empresa com uma única oficina. Durante a
construção, o escopo passou a incluir várias oficinas independentes na mesma
instalação: o SUPERADMIN cadastra as oficinas e cada uma tem seu próprio
ADMIN e seus mecânicos.

Com várias oficinas no mesmo banco, surgiram riscos concretos:

- Um usuário consultar ou alterar dados de outra oficina informando o ID de
  um recurso alheio (cliente, veículo, ordem de serviço).
- Um usuário enviar `oficinaId` de outra oficina nos filtros das listagens.
- Métricas financeiras de uma oficina ficarem visíveis para outra.
- CPF e placa únicos no sistema inteiro impedirem que o mesmo cliente seja
  atendido em duas oficinas diferentes.

## Decisão

1. **Banco compartilhado com discriminador `oficina_id`.** Não há um banco ou
   esquema por oficina. `Cliente`, `Usuario` e `OrdemServico` referenciam a
   `Oficina` (navegabilidade definida no ADR-0002).
2. **`TenantGuard` no backend** (`seguranca/TenantGuard`):
   - `obterOficinaDoSolicitante`: carrega a oficina do usuário autenticado a
     partir do banco, nunca de um parâmetro enviado pelo cliente.
   - `validarAcessoOficina`: compara a oficina do recurso com a do
     solicitante e responde **403** quando são diferentes.
3. **Filtros sobrescritos no servidor.** Para usuários vinculados a uma
   oficina, o `oficinaId` das listagens é sempre substituído pela oficina do
   solicitante, ignorando o valor recebido.
4. **Validação em leitura e em escrita.** Busca por ID, placa ou CPF,
   histórico do veículo, cadastro de veículo, aceitar, rejeitar, iniciar e
   finalizar OS, itens de peça e serviço, descontos e métricas do dashboard
   passam pelo `TenantGuard`.
5. **Unicidade por oficina.** O CPF do cliente é único dentro da oficina
   (`uk_cliente_oficina_cpf`), e não mais no sistema inteiro.
6. **SUPERADMIN transversal.** O SUPERADMIN não pertence a nenhuma oficina e
   tem acesso de consulta a todas; as rotas de escrita da operação da oficina
   continuam restritas a ADMIN e MECANICO.

## Consequências

Positivas:

- Isolamento garantido no servidor, independentemente do que o frontend
  enviar.
- Um único banco e uma única implantação para todas as oficinas, com
  manutenção simples.
- Regra concentrada em um componente reutilizável, coberta pela suíte
  `TenantIsolationTest` (cenários S1, S2, S3 e S7).

Custos:

- Todo novo serviço que expõe dados de oficina precisa chamar o
  `TenantGuard`; esquecer a chamada abre uma brecha. Revisões de código e
  testes de isolamento passam a ser obrigatórios para novas rotas.
- Cada requisição autenticada consulta o usuário e a oficina no banco.

## Alternativas consideradas

- **Banco ou esquema por oficina:** descartada pelo custo de implantação,
  migração e backup multiplicado pelo número de oficinas.
- **Filtro global do Hibernate ou row-level security no PostgreSQL:**
  descartada nesta versão. Exige configurar a sessão do banco a cada
  requisição e torna o comportamento menos explícito no código. Pode ser
  reavaliada se o número de entidades com escopo de oficina crescer.
- **Confiar no `oficinaId` enviado pelo frontend:** descartada por permitir
  acesso cruzado com uma simples alteração de parâmetro.

## Ajustes no diagrama

- Indicar a associação de `Cliente`, `Usuario` e `OrdemServico` com
  `Oficina` como obrigatória para os perfis ADMIN e MECANICO.
- Registrar a restrição de unicidade de CPF por oficina em `Cliente`.
- Manter uma nota UML ligada às classes afetadas: "Ver ADR-0003".
