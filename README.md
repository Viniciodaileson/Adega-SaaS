AdegaSaaS
Projeto desenvolvido para acompanhar minha evolução no curso de Análise e Desenvolvimento de Sistemas.

O objetivo é criar um sistema completo de gerenciamento para adegas, começando com um programa simples em Java e evoluindo passo a passo para um SaaS.
Objetivos
- Aprender Java e banco de dados.
- Estudar engenharia de software.
- Desenvolver APIs.
- Criar um sistema real.
- Registrar a evolução do projeto.
Tecnologias
- Java 21
- Maven
- Spring Boot 3.5
- Spring Data JPA
- H2
- Spring Security
- Nimbus JOSE + JWT (ES256)
- BouncyCastle (Argon2id)
- Bean Validation
- JUnit 5 e MockMvc
- React 19
- Vite 8
- Axios
- Git e GitHub
Funcionalidades da versão 2.2
- API REST para gerenciar bebidas: cadastro, listagem, consulta, atualização e remoção.
- Registro de vendas com baixa automática no estoque.
- Reposição de estoque.
- Validação dos dados de entrada com Bean Validation.
- Bloqueio de nomes duplicados, sem diferenciar maiúsculas de minúsculas.
- Uso de BigDecimal para evitar erros de arredondamento em valores monetários.
- Login com senha protegida por Argon2id.
- Access token assinado com ES256 (curva P-256), válido por 15 minutos.
- Refresh token opaco, com rotação e detecção de reutilização.
- Chave pública disponível em /.well-known/jwks.json.
- API stateless: todos os endpoints de bebidas exigem o cabeçalho Authorization: Bearer.
- Trilha de auditoria imutável, gravada de forma assíncrona.
- Limite de tentativas de login por IP: cinco falhas em 15 minutos bloqueiam o endereço por 15 minutos.
- Logout que revoga todos os refresh tokens do usuário.
- CORS restrito à origem do front-end.
- Front-end em React com tela de login e painel de gerenciamento.
- Tokens mantidos apenas na memória do navegador.
- Renovação automática do access token com rotação do refresh token.
Como executar
Pré-requisitos
- JDK 21 (java -version)
- Node.js 22 ou superior (node -v)
O Maven é executado pelo wrapper incluído no projeto.
Backend
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
A API ficará disponível em http://localhost:8080. O perfil dev habilita o console do H2. Sem esse perfil, o console permanece desativado.

Em produção, não use o perfil dev. Configure as seguintes variáveis de ambiente:
Variável	Finalidade
ADEGA_DB_URL	URL JDBC do banco de dados
ADEGA_DB_USER	Usuário do banco de dados
ADEGA_DB_PASSWORD	Senha do banco de dados
ADEGA_DDL_AUTO	Estratégia de schema: update, validate ou none
ADEGA_JWT_CHAVE_JWK	Chave privada ES256 em formato JWK; recomendada em produção
ADEGA_JWT_ARQUIVO	Caminho do arquivo da chave; padrão: adega-jwt.jwk
ADEGA_CORS_ORIGENS	Origens do front-end, separadas por vírgula
ADEGA_ADMIN_SENHA	Senha inicial do usuário administrador


Front-end
Em outro terminal:
cd frontend
npm install
npm run dev
O painel ficará disponível em http://localhost:5173.

No VS Code, use Ctrl+Shift+B e selecione a tarefa Executar AdegaSaaS, ou pressione F5. Para iniciar o front-end, abra Terminal > Executar Tarefa e selecione Executar front-end. Para interromper a execução, use Ctrl+C no terminal.
Usuário administrador
Na primeira execução, o sistema cria automaticamente o usuário admin.

A senha pode ser definida antes de iniciar a aplicação. Ela deve ter pelo menos oito caracteres:
adega.admin.senha=MinhaSenhaForte@2026
Também é possível defini-la por variável de ambiente no PowerShell:
$env:ADEGA_ADMIN_SENHA = "MinhaSenhaForte@2026"
.\mvnw.cmd spring-boot:run
Se nenhuma senha for definida, o sistema gera uma senha aleatória e a exibe uma única vez no console. Guarde-a, pois ela não será exibida novamente.

Se esquecer a senha, pare a API, apague adega.mv.db e inicie a aplicação novamente.
Front-end
O front-end fica na pasta frontend/ e se comunica com a API em http://localhost:8080.

Para apontá-lo para outra API, copie frontend/.env.example para frontend/.env.local e ajuste VITE_API_URL. Arquivos .env.local não são versionados.
Estrutura do front-end
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
Como funciona a sessão
- O accessToken e o refreshToken ficam na memória do navegador, em src/servicos/tokens.js.
- Os tokens não são gravados em localStorage, sessionStorage nem em cookies.
- Recarregar a página encerra a sessão e retorna o usuário à tela de login. Esse comportamento é intencional e segue a arquitetura stateless do backend.
- O Axios adiciona Authorization: Bearer <token> às chamadas.
- Quando a API responde com 401, o cliente tenta renovar os tokens usando o refresh token e repete a requisição original uma única vez.
- Se a renovação falhar, os tokens são apagados e o usuário retorna à tela de login.
- O botão Sair chama POST /api/auth/logout, revoga todos os refresh tokens do usuário e limpa os tokens da memória.
Painel
- Tabela com ID, nome, preço, quantidade em estoque e valor total em estoque.
- Formulário para cadastrar bebidas.
- Ação Vender 1 Unidade em cada linha.
- Ação Repor Estoque em cada linha, com quantidade informada em uma caixa de diálogo.
- Cartões de resumo com o total de bebidas, unidades em estoque e valor total.
- Mensagens de erro do backend apresentadas em linguagem amigável.
Autenticação pela API
Login
$login = Invoke-RestMethod -Uri http://localhost:8080/api/auth/login -Method Post -ContentType "application/json" -Body '{"login":"admin","senha":"MinhaSenhaForte@2026"}'
$login.accessToken
$login.refreshToken
$login.expiresIn
Usar o access token
$headers = @{ Authorization = "Bearer $($login.accessToken)" }
Invoke-RestMethod -Uri http://localhost:8080/api/bebidas -Headers $headers
Renovar os tokens
$renovado = Invoke-RestMethod -Uri http://localhost:8080/api/auth/refresh -Method Post -ContentType "application/json" -Body "{`"refreshToken`":`"$($login.refreshToken)`"}"
Cada refresh token só pode ser usado uma vez. Se ele for enviado novamente, todos os refresh tokens do usuário são revogados imediatamente.
Encerrar a sessão
Invoke-RestMethod -Uri http://localhost:8080/api/auth/logout -Method Post -ContentType "application/json" -Body "{`"refreshToken`":`"$($login.refreshToken)`"}"
O endpoint responde com 204 No Content e revoga todos os refresh tokens do usuário.
Consultar a chave pública
Invoke-RestMethod -Uri http://localhost:8080/.well-known/jwks.json
Rotas
Método	Caminho	Acesso
POST	/api/auth/login	Público
POST	/api/auth/refresh	Público
POST	/api/auth/logout	Público
GET	/.well-known/jwks.json	Público
GET	/api/bebidas	Token
GET	/api/bebidas/{id}	Token
POST	/api/bebidas	Token
PUT	/api/bebidas/{id}	Token
DELETE	/api/bebidas/{id}	Token
POST	/api/bebidas/{id}/venda	Token
POST	/api/bebidas/{id}/reposicao	Token


Segurança
- Assinatura ES256: utiliza um par de chaves EC na curva secp256r1. A chave privada fica em adega-jwt.jwk, arquivo ignorado pelo Git e reutilizado entre reinicializações. Em produção, pode ser fornecida por ADEGA_JWT_CHAVE_JWK. A chave pública fica disponível no JWKS.
- Console do H2: permanece desativado por padrão. Só é habilitado no perfil dev, que também libera a rota /h2-console/**. O console não deve ser exposto em produção.
- Credenciais do banco: URL, usuário e senha são configurados por variáveis de ambiente. A senha vazia do H2 é apenas um padrão local.
- Usuários inativos: o filtro JWT consulta o banco para confirmar que o usuário ainda existe e está ativo.
- Respostas de autenticação: login, refresh e logout enviam Cache-Control: no-store e Pragma: no-cache.
- Limites de tamanho: tokens de acesso maiores que 4 KB são ignorados; refresh tokens têm limite de 200 caracteres.
- Cabeçalhos HTTP: incluem X-Content-Type-Options: nosniff, Referrer-Policy: no-referrer, frame-ancestors restrito e HSTS em HTTPS.
- Dependências: Nimbus JOSE + JWT 10.10 e BouncyCastle 1.86.
- Tratamento de erros: detalhes técnicos de erros inesperados vão para o log, não para o cliente.
- Limpeza de tokens: refresh tokens expirados ou revogados há mais de 30 dias são removidos diariamente.
- Senhas: protegidas com Argon2id, salt de 16 bytes, hash de 32 bytes, paralelismo 1, memória de 64 MB e três iterações.
- Refresh token opaco: gerado com 32 bytes aleatórios por SecureRandom e enviado em Base64 URL-safe. O banco armazena apenas o hash SHA-256.
- Rotação do refresh token: cada uso gera um novo token e revoga o anterior. A coluna substituido_por mantém o vínculo entre eles.
- Detecção de reutilização: se um token revogado for apresentado, todos os tokens do usuário são revogados.
- Proteção contra corrida: a leitura do refresh token usa PESSIMISTIC_WRITE.
- Validade: access tokens duram 15 minutos e refresh tokens, sete dias.
- Sessão stateless: não usa cookie de sessão nem CSRF.
- Auditoria: a aplicação permite apenas inserções na entidade, anotada com @Immutable, e o repositório expõe apenas save. Isso não substitui permissões no banco que impeçam operações diretas de UPDATE ou DELETE. A auditoria é gravada de forma assíncrona: o filtro publica um evento e um listener com @Async o persiste.
- Respostas genéricas no login: login inexistente e senha incorreta retornam a mesma mensagem. Uma verificação com hash fictício ajuda a reduzir diferenças no tempo de resposta.
- Limite por IP: cinco tentativas inválidas em 15 minutos bloqueiam o IP por 15 minutos, com resposta 429 Too Many Requests.
- Logout: o refresh token apresentado é localizado e todos os tokens do usuário são revogados.
- CORS: por padrão, permite apenas http://localhost:5173 e http://127.0.0.1:5173. As origens podem ser configuradas por ADEGA_CORS_ORIGENS; métodos e cabeçalhos são explícitos e credenciais não são permitidas.
- Tokens no front-end: ficam apenas na memória, sem persistência local, o que reduz o impacto de XSS sobre tokens armazenados.
Testes
Execute os testes com:
.\mvnw.cmd test
O projeto tem 39 testes: 11 do modelo, 13 dos endpoints de bebidas e 15 de autenticação e segurança. Os testes usam H2 em memória.
Estrutura do projeto
AdegaSaaS/
├── pom.xml
├── mvnw
├── mvnw.cmd
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
│   └── service/BebidaService.java
├── src/main/java/com/adega/security/
│   ├── auth/
│   │   ├── AuthController.java
│   │   ├── AuthService.java
│   │   ├── LimiteTentativasFilter.java
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   └── RefreshRequest.java
│   ├── audit/
│   │   ├── AuditFilter.java
│   │   ├── AuditLog.java
│   │   ├── AuditLogEvent.java
│   │   ├── AuditLogRepository.java
│   │   └── AuditoriaListener.java
│   ├── config/
│   │   ├── AsyncConfig.java
│   │   └── SecurityConfig.java
│   ├── exception/
│   ├── jwt/
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtKeyProvider.java
│   │   ├── JwtProperties.java
│   │   └── JwtService.java
│   ├── refresh/
│   │   ├── RefreshToken.java
│   │   ├── RefreshTokenRepository.java
│   │   ├── RefreshTokenService.java
│   │   └── RotacaoRefreshToken.java
│   └── user/
│       ├── CargaInicialAdmin.java
│       ├── PasswordEncoderConfig.java
│       ├── Usuario.java
│       └── UsuarioRepository.java
├── src/main/resources/
│   ├── application.properties
│   └── application-dev.properties
├── src/test/java/com/adega/
│   ├── api/BebidaControllerTest.java
│   ├── model/BebidaTest.java
│   └── security/auth/AutenticacaoControllerTest.java
└── adega.mv.db
Banco de dados
O arquivo adega.mv.db é criado automaticamente na raiz do projeto na primeira execução.

O Hibernate cria as seguintes tabelas:
- bebidas
- usuarios
- refresh_tokens
- logs_auditoria
O console do H2 fica disponível em http://localhost:8080/h2-console apenas no perfil dev:
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
Dados de acesso:
- JDBC URL: jdbc:h2:file:./adega
- Usuário: sa
- Senha: vazia
O console oferece acesso completo ao banco. Por isso, fica desativado por padrão (spring.h2.console.enabled=false) e nunca deve ser habilitado em produção. Para depuração local, use --spring.profiles.active=dev.
Roadmap
- v0.1 — Cadastro e venda inicial
- v0.2 — Refatoração para orientação a objetos
- v1.0 — Menu completo, validações e encapsulamento
- v1.1 — BigDecimal, identificador e modelo sem impressão
- v1.2 — Persistência com SQLite e JDBC
- v1.3 — Maven e separação em camadas
- v1.4 — Testes automatizados com JUnit
- v2.0 — API REST com Spring Boot
- v2.1 — Login, JWT ES256, Argon2id, refresh rotativo e auditoria
- v2.2 — Front-end React, CORS, limite por IP e logout com revogação
- v2.3 — Edição e exclusão de bebidas no painel
Próximos passos de segurança
- Armazenar a chave privada em um cofre ou KMS em produção. Atualmente, o padrão é o arquivo local adega-jwt.jwk; ADEGA_JWT_CHAVE_JWK permite fornecê-la por ambiente.
- Servir a API somente por HTTPS e configurar o HSTS definitivo.
- Adicionar limite de tentativas por conta, além do limite por IP, para conter ataques distribuídos.
- Migrar o limite de tentativas para um armazenamento distribuído, como Redis, em cenários com várias instâncias.
- Considerar X-Forwarded-For apenas quando houver um proxy reverso confiável (server.forward-headers-strategy=framework).
- Substituir ddl-auto=update por migrações versionadas com Flyway ou Liquibase.
- Adicionar verificação de e-mail, política de troca de senha e TOTP.
- Restringir o CORS e a origem do front-end por ambiente.
- Adicionar uma Content Security Policy (CSP) ao front-end em produção.
Projeto iniciado em julho de 2026.