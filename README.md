# 🍷 AdegaSaaS

Projeto desenvolvido para documentar e consolidar o aprendizado no curso de Análise e Desenvolvimento de Sistemas. 

O sistema evoluiu de um programa simples em Java para uma solução SaaS estruturada. O foco do projeto contempla a divisão clara de responsabilidades em camadas, segurança defensiva aplicada a APIs e persistência confiável integrada a uma interface responsiva.

---

## Objetivos do Projeto

*   Desenvolver o domínio da linguagem Java e modelagem de bancos de dados relacionais.
*   Praticar padrões de arquitetura de software e design de código limpo.
*   Construir APIs REST completas e seguras.
*   Criar um produto de software funcional aplicável a cenários reais.
*   Versionar de forma incremental cada etapa de evolução do sistema.

---

## Tecnologias Utilizadas

*   **Back-End:** Java 21, Spring Boot 3.5, Spring Data JPA, Maven (via wrapper integrado).
*   **Front-End:** React 19, Vite 8, Axios para consumo de recursos assíncronos.
*   **Banco de Dados:** H2 Database (execução baseada em arquivo local para desenvolvimento).
*   **Segurança e Criptografia:** Spring Security, Nimbus JOSE + JWT (algoritmo ES256), BouncyCastle (algoritmo Argon2id), Bean Validation.
*   **Testes Automatizados:** JUnit 5, MockMvc.

---

## Funcionalidades Cadastradas

*   **Gerenciamento de Bebidas:** API REST completa para cadastro, listagem, consulta, atualização e remoção de produtos.
*   **Operações de Estoque:** Registro de vendas com baixa automatizada e funcionalidade de reposição de unidades.
*   **Precisão Monetária:** Controle financeiro baseado em `BigDecimal` para evitar inconsistências de arredondamento.
*   **Segurança de Escrita:** Bloqueio de nomes duplicados com tratamento de caixa (case-insensitive) e validações estruturais via Bean Validation.
*   **Cadeia de Autenticação Robusta:** Login protegido por hashing Argon2id. Emissão de Access Token JWT com assinatura assimétrica ES256 (curva P-256) válido por 15 minutos e Refresh Token opaco rotativo com validade de 7 dias.
*   **Proteção de Infraestrutura:** Filtro para controle de tentativas de login por IP (5 falhas em 15 minutos bloqueiam o endereço temporariamente) e auditoria imutável processada de forma assíncrona.
*   **Sessão Segura no Cliente:** Interface em React que mantém os tokens exclusivamente em memória RAM, anulando riscos de persistência indevida em disco (XSS). Renovação automática de chaves sem deslogar o usuário.

---

## Como Executar a Aplicação

### Pré-requisitos
Certifique-se de possuir o **JDK 21** e o **Node.js 22** (ou superiores) instalados no ambiente de execução.

### Inicialização do Back-End
Execute o comando abaixo na raiz do projeto para subir o servidor:
```bash
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```
A API iniciará no endereço `http://localhost:8080`. 
*Nota: O perfil `dev` ativa o console do banco H2 em `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./adega`, Usuário: `sa`, Senha: em branco). Em ambientes produtivos, este perfil deve ser omitido.*

**Credenciais do Administrador:**
O usuário `admin` é gerado na primeira inicialização. A senha pode ser injetada via arquivo de propriedades (`adega.admin.senha=SuaSenha`) ou por variável de ambiente. Caso nenhuma senha seja definida, o Spring Boot imprimirá uma chave aleatória temporária no console.

### Inicialização do Front-End
Em um terminal paralelo, acesse o diretório do cliente e inicie o servidor do Vite:
```bash
cd frontend
npm install
npm run dev
```
A interface gráfica estará acessível em `http://localhost:5173`.

---

## Estrutura do Front-End

O código da interface está organizado para isolar a renderização visual das regras de comunicação HTTP:
```text
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

---

## Mapeamento de Rotas da API

| Método | Caminho | Restrição de Acesso |
| :--- | :--- | :--- |
| **POST** | `/api/auth/login` | Público |
| **POST** | `/api/auth/refresh` | Público |
| **POST** | `/api/auth/logout` | Público |
| **GET** | `/.well-known/jwks.json` | Público |
| **GET** | `/api/bebidas` | Exige Token Bearer |
| **GET** | `/api/bebidas/{id}` | Exige Token Bearer |
| **POST** | `/api/bebidas` | Exige Token Bearer |
| **PUT** | `/api/bebidas/{id}` | Exige Token Bearer |
| **DELETE** | `/api/bebidas/{id}` | Exige Token Bearer |
| **POST** | `/api/bebidas/{id}/venda` | Exige Token Bearer |
| **POST** | `/api/bebidas/{id}/reposicao` | Exige Token Bearer |

---

## Governança e Segurança Aplicada

*   **API Stateless:** Ausência completa de cookies ou sessões ativas no servidor. Cada requisição de recurso exige a validação individual do cabeçalho `Authorization: Bearer`.
*   **Tratamento de Tokens:** Implementação de rotação obrigatória para cada Refresh Token gerado. Caso um token revogado tente ser reutilizado, o sistema invalida imediatamente toda a família de chaves daquele usuário.
*   **Proteção de Chaves:** Assinatura baseada em chaves criptográficas EC geradas localmente em arquivo protegido ou injetadas via variáveis de ambiente (`ADEGA_JWT_CHAVE_JWK`), sem exposição de segredos privados no JWKS público.
*   **Blindagem de Erros:** Exclusão de mensagens internas, pilhas de execução (stacktraces) ou dados de binding em respostas de erro enviadas ao cliente, mitigando vazamento de informações de infraestrutura.
