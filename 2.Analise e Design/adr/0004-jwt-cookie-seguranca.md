# ADR-0004: Sessão stateless via JWT em cookie HttpOnly, com revogação ativa e proteção contra força bruta

Status: Aceita
Data: 2026-10-26
Relacionado: ADR-0003 (multi-tenant)

## Contexto

O frontend é uma aplicação Next.js (React) que consome a API Spring Boot.
A autenticação precisa:

- Evitar que o token de sessão seja lido por scripts da página. Guardar o
  token em `localStorage` ou `sessionStorage` o expõe a roubo via XSS.
- Refletir imediatamente a desativação de um usuário ou de uma oficina, sem
  esperar o token expirar.
- Refletir mudanças de perfil feitas no banco.
- Resistir a tentativas repetidas de senha sem esgotar a memória do servidor.

## Decisão

1. **JWT assinado, enviado em cookie.** O login (`POST /auth/login`) devolve
   o cookie `jwt` com `HttpOnly`, `SameSite=Lax`, `Path=/` e validade de
   1 hora. O atributo `Secure` é controlado pela propriedade
   `app.security.cookie-secure` e deve ficar ativo em produção (HTTPS).
   O token carrega `id`, e-mail (`sub`) e perfil (`role`).
2. **Logout** (`POST /auth/logout`) sobrescreve o cookie com `Max-Age=0`.
3. **Filtro `JwtCookieFilter`.** Lê o token do cookie `jwt` (ou do cabeçalho
   `Authorization: Bearer`, usado em ferramentas de teste) e, a cada
   requisição:
   - recarrega o usuário com a oficina (`findComOficinaById`);
   - rejeita a autenticação se o usuário estiver inativo, ou se a oficina
     dele estiver inativa (exceto SUPERADMIN);
   - usa o perfil atual do banco, e não o gravado no token.
4. **Bloqueio por tentativas (`LoginAttemptService`).** Após 5 tentativas
   consecutivas com senha errada, o e-mail fica bloqueado por 15 minutos.
   O cache de tentativas tem capacidade máxima (`MAX_ENTRIES` = 10.000):
   ao atingi-la, remove os registros sem bloqueio ativo e, se ainda estiver
   cheio, descarta um quarto das entradas restantes.
5. **Confirmação de senha atual.** Alterar o próprio e-mail ou a senha em
   "Meus Dados" exige o campo `senhaAtual`.

## Consequências

Positivas:

- O token não fica acessível a JavaScript, o que reduz o impacto de XSS.
- Desativar um usuário ou uma oficina corta o acesso na requisição seguinte.
- A API continua stateless: não há sessão guardada no servidor.
- Ataques de força bruta não conseguem esgotar a memória do cache de
  tentativas.

Custos:

- Cada requisição autenticada faz uma consulta ao banco para validar o
  usuário e a oficina.
- Cookies exigem cuidado com CORS (`allowCredentials`) e com a origem do
  frontend; o ambiente de desenvolvimento usa o proxy do Next.js.
- `SameSite=Lax` não substitui proteção CSRF em todos os cenários; as
  operações de escrita usam métodos não-GET e origens permitidas pelo CORS.

## Alternativas consideradas

- **Token em `localStorage` com cabeçalho `Authorization`:** descartada pela
  exposição a XSS.
- **Sessão HTTP no servidor (`JSESSIONID`):** descartada para manter a API
  stateless e simples de escalar.
- **Lista de revogação de tokens:** desnecessária, porque a validação do
  usuário e da oficina a cada requisição já cobre a desativação.
