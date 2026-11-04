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
- Git e GitHub

## Funcionalidades (v2.1)

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
- Testes automatizados do modelo, dos endpoints e da segurança (36 testes)

## Como executar

Pré-requisitos: JDK 21 (`java -version`). O Maven vem pelo wrapper, não precisa instalar.

```powershell
.\mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`.

No VS Code, use `Ctrl+Shift+B` (tarefa **Executar AdegaSaaS**) ou `F5`. Para parar, `Ctrl+C` no terminal.

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

### Chave pública (JWKS)

```powershell
Invoke-RestMethod -Uri http://localhost:8080/.well-known/jwks.json
```

### Rotas

| Método | Caminho | Acesso |
|---|---|---|
| POST | `/api/auth/login` | Público |
| POST | `/api/auth/refresh` | Público |
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

## Testes

```powershell
.\mvnw.cmd test
```

São 36 testes: 11 do modelo, 13 dos endpoints de bebidas e 12 de autenticação e segurança. Os testes usam H2 em memória.

## Estrutura

```
AdegaSaaS/
├── pom.xml
├── mvnw / mvnw.cmd
├── .mvn/wrapper/maven-wrapper.properties
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
- [ ] v2.2 — Front-end web com React

## Próximos passos de segurança

- Limitar tentativas de login por IP e por usuário
- Configurar CORS para o front-end
- Persistir a chave privada em cofre ou KMS (hoje ela fica em memória e muda a cada reinício)
- Servir a API somente por HTTPS
- Criar rotina de limpeza de refresh tokens expirados
- Endpoint de logout com revogação dos tokens

---

Projeto iniciado em Julho de 2026.
