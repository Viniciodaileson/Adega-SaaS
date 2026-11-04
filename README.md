# 🍷 AdegaSaaS

Projeto desenvolvido para acompanhar minha evolução no curso de Análise e Desenvolvimento de Sistemas.

O objetivo é criar um sistema completo de gerenciamento para adegas, iniciando com um programa simples em Java e evoluindo, passo a passo, para um SaaS profissional.

## Objetivos

- Aprender Java
- Aprender Banco de Dados
- Aprender Engenharia de Software
- Desenvolver APIs
- Criar um sistema real
- Registrar toda a evolução do projeto

## Tecnologias

- Java 21
- Maven
- Spring Boot 3.5
- Spring Data JPA
- H2 (banco de dados)
- Spring Security
- Nimbus JOSE + JWT (ES256)
- BouncyCastle (Argon2id)
- Bean Validation
- JUnit 5 + MockMvc
- React 19
- Vite 8
- Axios
- Git e GitHub

## Funcionalidades (v2.2)

- API REST para gerenciar bebidas
- Cadastro, listagem, busca, atualização e remoção
- Venda com baixa automática no estoque
- Reposição de estoque
- Validação dos dados de entrada com Bean Validation
- Bloqueio de nome duplicado, ignorando maiúsculas e minúsculas
- Preço com `BigDecimal` (sem erro de arredondamento em dinheiro)
- Login com senha protegida por Argon2id
- Access token assinado com ES256 (curva P-256) e validade de 15 minutos
- Refresh token opaco com rotação e detecção de reuso
- Chave pública exposta em `/.well-known/jwks.json`
- API stateless: todo endpoint de bebidas exige `Authorization: Bearer`
- Trilha de auditoria imutável gravada de forma assíncrona
- Limite de tentativas de login por IP (5 falhas em 15 minutos bloqueiam por 15 minutos)
- Logout com revogação de todos os refresh tokens do usuário
- CORS restrito ao endereço do front-end
- Front-end React com tela de login e painel de gerenciamento
- Tokens mantidos apenas na memória do navegador
- Renovação automática do access token com rotação do refresh token
- Testes automatizados do modelo, dos endpoints e da segurança (38 testes)

## Como executar

Pré-requisitos: JDK 21 (`java -version`) e Node.js 22 ou superior (`node -v`). O Maven vem pelo wrapper, não precisa instalar.

### Backend

```powershell
.\mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`.

### Front-end

Em outro terminal:

```powershell
cd frontend
npm install
npm run dev
```

O painel abre em `http://localhost:5173`.

No VS Code, use `Ctrl+Shift+B` (tarefa **Executar AdegaSaaS**) ou `F5`. Para o front-end, use **Terminal → Executar Tarefa** e escolha **Executar front-end**. Para parar, `Ctrl+C` no terminal.

### Usuário administrador

Na primeira execução, o usuário `admin` é criado automaticamente.

A senha pode ser definida antes de subir a aplicação:

```properties
adega.admin.senha=MinhaSenhaForte@2026
```

Ou por variável de ambiente:

```powershell
$env:ADEGA_ADMIN_SENHA = "MinhaSenhaForte@2026"
.\mvnw.cmd spring-boot:run
```

Se nenhuma senha for definida, uma senha aleatória é gerada e exibida **uma única vez** no console:

```
============================================================
 USUARIO ADMINISTRADOR CRIADO
 Login: admin
 Senha: <senha gerada>
 Guarde esta senha. Ela nao sera exibida novamente.
============================================================
```

Se esquecer a senha, pare a API, apague `adega.mv.db` e suba de novo.

## Front-end

O front-end fica na pasta `frontend/` e conversa com a API em `http://localhost:8080`.

### Estrutura

```
frontend/
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── main.jsx
    ├── App.jsx
    ├── estilos.css
    ├── componentes/
    │   ├── Login.jsx
    │   └── PainelBebidas.jsx
    └── servicos/
        ├── api.js
        ├── tokens.js
        ├── erros.js
        └── formatacao.js
```

### Como a sessão funciona

- O `accessToken` e o `refreshToken` ficam **apenas na memória** do navegador, em um módulo próprio (`src/servicos/tokens.js`).
- Nada é gravado em `localStorage`, `sessionStorage` ou cookies.
- Recarregar a página (F5) encerra a sessão e volta para o login. Isso é intencional e alinhado à arquitetura stateless do backend.
- O Axios anexa `Authorization: Bearer <token>` em todas as chamadas.
- Quando a API responde `401`, o cliente tenta renovar o par de tokens usando o refresh token e repete a requisição original uma única vez.
- Se a renovação falhar, os tokens são apagados e o usuário volta para a tela de login.
- O botão **Sair** chama `POST /api/auth/logout`, revoga todos os refresh tokens do usuário e limpa a memória.

### Painel

- Tabela com ID, Nome, Preço, Quantidade em Estoque e Valor Total em Estoque.
- Formulário para cadastrar nova bebida.
- Ação **Vender 1 Unidade** em cada linha.
- Ação **Repor Estoque** em cada linha, com a quantidade informada em uma caixa de diálogo.
- Cartões de resumo com total de bebidas, unidades em estoque e valor total.
- Mensagens de erro retornadas pelo backend traduzidas para linguagem amigável.

## Autenticação

### Login

```powershell
$login = Invoke-RestMethod -Uri http://localhost:8080/api/auth/login -Method Post -ContentType "application/json" -Body '{"login":"admin","senha":"MinhaSenhaForte@2026"}'
$login.accessToken
$login.refreshToken
$login.expiresIn
```

### Usar o access token

```powershell
$headers = @{ Authorization = "Bearer $($login.accessToken)" }
Invoke-RestMethod -Uri http://localhost:8080/api/bebidas -Headers $headers
```

### Renovar o par de tokens

```powershell
$renovado = Invoke-RestMethod -Uri http://localhost:8080/api/auth/refresh -Method Post -ContentType "application/json" -Body "{`"refreshToken`":`"$($login.refreshToken)`"}"
```

Cada refresh token só pode ser usado uma vez. Se o mesmo token for enviado de novo, todos os refresh tokens daquele usuário são revogados imediatamente.

### Encerrar a sessão

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/auth/logout -Method Post -ContentType "application/json" -Body "{`"refreshToken`":`"$($login.refreshToken)`"}"
```

O endpoint responde `204 No Content` e revoga todos os refresh tokens do usuário.

### Chave pública (JWKS)

```powershell
Invoke-RestMethod -Uri http://localhost:8080/.well-known/jwks.json
```

### Rotas

| Método | Caminho | Acesso |
|---|---|---|
| POST | `/api/auth/login` | Público |
| POST | `/api/auth/refresh` | Público |
| POST | `/api/auth/logout` | Público |
| GET | `/.well-known/jwks.json` | Público |
| GET | `/api/bebidas` | Token |
| GET | `/api/bebidas/{id}` | Token |
| POST | `/api/bebidas` | Token |
| PUT | `/api/bebidas/{id}` | Token |
| DELETE | `/api/bebidas/{id}` | Token |
| POST | `/api/bebidas/{id}/venda` | Token |
| POST | `/api/bebidas/{id}/reposicao` | Token |

## Segurança

- **Assinatura ES256**: par de chaves EC gerado na curva `secp256r1`. A chave privada nunca sai da aplicação; a pública fica disponível no JWKS.
- **Senhas com Argon2id**: salt de 16 bytes, hash de 32 bytes, paralelismo 1, memória de 64 MB e 3 iterações.
- **Refresh token opaco**: 32 bytes aleatórios com `SecureRandom`, enviados em Base64 URL-safe. O banco guarda apenas o hash SHA-256.
- **Rotação de refresh token**: cada uso gera um novo token e revoga o anterior. A coluna `substituido_por` mantém a corrente.
- **Detecção de reuso**: se um token revogado for apresentado, todos os tokens do usuário são revogados.
- **Bloqueio contra corrida**: a leitura do refresh token usa `PESSIMISTIC_WRITE`.
- **Tempo de vida**: access token de 15 minutos e refresh token de 7 dias.
- **Sessão stateless**: sem cookie de sessão e sem CSRF.
- **Cabeçalhos**: `X-Content-Type-Options: nosniff`, HSTS (em HTTPS) e frame restrito.
- **Auditoria imutável**: a entidade é anotada com `@Immutable` e o repositório expõe apenas `save`.
- **Auditoria assíncrona**: o filtro publica um evento e um listener com `@Async` grava em segundo plano.
- **Resposta genérica no login**: login inexistente e senha errada devolvem a mesma mensagem, com verificação de hash fictício para reduzir diferença de tempo.
- **Limite por IP**: 5 tentativas de login inválidas em 15 minutos bloqueiam o IP por 15 minutos, com resposta `429 Too Many Requests`.
- **Logout com revogação**: o refresh token apresentado é localizado e todos os tokens do usuário são revogados.
- **CORS restrito**: apenas `http://localhost:5173` e `http://127.0.0.1:5173`, com métodos e cabeçalhos explícitos e sem credenciais.
- **Tokens no front-end apenas em memória**: sem persistência local, reduzindo o impacto de XSS sobre tokens armazenados.

## Testes

```powershell
.\mvnw.cmd test
```

São 38 testes: 11 do modelo, 13 dos endpoints de bebidas e 14 de autenticação e segurança. Os testes usam H2 em memória.

## Estrutura

```
AdegaSaaS/
├── pom.xml
├── mvnw / mvnw.cmd
├── .mvn/wrapper/maven-wrapper.properties
├── frontend/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.jsx
│       ├── App.jsx
│       ├── estilos.css
│       ├── componentes/
│       │   ├── Login.jsx
│       │   └── PainelBebidas.jsx
│       └── servicos/
│           ├── api.js
│           ├── tokens.js
│           ├── erros.js
│           └── formatacao.js
├── src/main/java/com/adega/
│   ├── AdegaApplication.java
│   ├── api/
│   │   ├── BebidaController.java
│   │   └── ApiExceptionHandler.java
│   ├── dto/
│   ├── exception/
│   ├── model/Bebida.java
│   ├── repository/BebidaRepository.java
│   ├── service/BebidaService.java
│   └── security/
│       ├── auth/
│       │   ├── AuthController.java
│       │   ├── AuthService.java
│       │   ├── LimiteTentativasFilter.java
│       │   ├── LoginRequest.java
│       │   ├── LoginResponse.java
│       │   └── RefreshRequest.java
│       ├── audit/
│       │   ├── AuditFilter.java
│       │   ├── AuditLog.java
│       │   ├── AuditLogEvent.java
│       │   ├── AuditLogRepository.java
│       │   └── AuditoriaListener.java
│       ├── config/
│       │   ├── AsyncConfig.java
│       │   └── SecurityConfig.java
│       ├── exception/
│       ├── jwt/
│       │   ├── JwtAuthenticationFilter.java
│       │   ├── JwtKeyProvider.java
│       │   ├── JwtProperties.java
│       │   └── JwtService.java
│       ├── refresh/
│       │   ├── RefreshToken.java
│       │   ├── RefreshTokenRepository.java
│       │   ├── RefreshTokenService.java
│       │   └── RotacaoRefreshToken.java
│       └── user/
│           ├── CargaInicialAdmin.java
│           ├── PasswordEncoderConfig.java
│           ├── Usuario.java
│           └── UsuarioRepository.java
├── src/main/resources/application.properties
├── src/test/java/com/adega/
│   ├── api/BebidaControllerTest.java
│   ├── model/BebidaTest.java
│   └── security/auth/AutenticacaoControllerTest.java
└── adega.mv.db
```

## Banco de dados

O arquivo `adega.mv.db` é criado automaticamente na primeira execução, na raiz do projeto.

Tabelas geradas pelo Hibernate:

- `bebidas`
- `usuarios`
- `refresh_tokens`
- `logs_auditoria`

O console do H2 fica em `http://localhost:8080/h2-console`:

- JDBC URL: `jdbc:h2:file:./adega`
- Usuário: `sa`
- Senha: vazia

O console é útil apenas em desenvolvimento. Em produção, desligue com `spring.h2.console.enabled=false`.

## Roadmap

- [x] v0.1 — Cadastro e venda inicial
- [x] v0.2 — Refatoração para orientação a objetos
- [x] v1.0 — Menu completo, validações e encapsulamento
- [x] v1.1 — `BigDecimal`, `id` e modelo sem impressão
- [x] v1.2 — Persistência com SQLite + JDBC
- [x] v1.3 — Maven e separação em camadas
- [x] v1.4 — Testes automatizados com JUnit
- [x] v2.0 — API REST com Spring Boot
- [x] v2.1 — Login, JWT ES256, Argon2id, refresh rotativo e auditoria
- [x] v2.2 — Front-end React, CORS, limite por IP e logout com revogação
- [ ] v2.3 — Edição e exclusão de bebidas no painel

## Próximos passos de segurança

- Persistir a chave privada em cofre ou KMS (hoje ela fica em memória e muda a cada reinício)
- Servir a API somente por HTTPS
- Criar rotina de limpeza de refresh tokens expirados
- Migrar o limite de tentativas para um armazenamento distribuído (Redis) em cenário com múltiplas instâncias
- Adicionar verificação de e-mail e política de troca de senha
- Avaliar autenticação em dois fatores (TOTP)

---

Projeto iniciado em Julho de 2026.
