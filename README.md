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
- SQLite (via JDBC)
- Git
- GitHub

## Funcionalidades (v1.3)

- Cadastro de bebidas com `id`, nome, preço e quantidade em estoque
- Consulta do estoque com valor total
- Reposição de estoque
- Venda com baixa automática no estoque
- Validação de dados (nome, preço, quantidade e entradas inválidas)
- Bloqueio de bebidas com nome duplicado (na aplicação e no banco)
- Preço com `BigDecimal` (sem erro de arredondamento em dinheiro)
- Dados salvos em SQLite: nada se perde ao fechar o programa
- Código organizado em camadas: `model`, `repository`, `service` e `ui`

## Como executar

Pré-requisitos: JDK 21 (`java -version`). O Maven vem pelo wrapper, não precisa instalar.

```powershell
# Compila
.\mvnw.cmd compile

# Executa
.\mvnw.cmd exec:java
```

Na primeira execução o wrapper baixa o Maven e o driver do SQLite (precisa de internet).

No VS Code, com a **Extension Pack for Java**, o projeto é reconhecido como Maven automaticamente.

## Estrutura

```
AdegaSaaS/
├── pom.xml
├── mvnw / mvnw.cmd
├── .mvn/wrapper/maven-wrapper.properties
├── src/main/java/com/adega/
│   ├── AdegaApplication.java        # Ponto de entrada
│   ├── model/
│   │   ├── Bebida.java              # Estado e regras de uma bebida
│   │   └── Resultado.java           # Sucesso/falha + mensagem + valor
│   ├── repository/
│   │   ├── ConexaoBanco.java        # Conexão e criação da tabela
│   │   └── BebidaRepository.java    # SQL (INSERT, SELECT, UPDATE)
│   ├── service/
│   │   └── BebidaService.java       # Orquestra repositório + regras
│   └── ui/
│       └── CadastroBebidas.java     # Menu de console
└── adega.db                         # Banco (gerado, não versionado)
```

## Camadas

- `model`: o que é uma bebida e o que ela sabe fazer (vender, repor, calcular total).
- `repository`: como os dados são gravados e lidos do banco.
- `service`: junta as duas coisas — busca, valida duplicidade, decide quando salvar.
- `ui`: conversa com o usuário. Não sabe SQL e não conhece o banco.

## Banco de dados

O arquivo `adega.db` é criado automaticamente na primeira execução, na raiz do projeto.

```sql
CREATE TABLE bebidas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL COLLATE NOCASE UNIQUE,
    preco TEXT NOT NULL,
    quant_estoque INTEGER NOT NULL CHECK (quant_estoque >= 0)
);
```

Para começar do zero, apague `adega.db` e rode de novo.

## Roadmap

- [x] v0.1 — Cadastro e venda inicial
- [x] v0.2 — Refatoração para orientação a objetos
- [x] v1.0 — Menu completo, validações e encapsulamento
- [x] v1.1 — `BigDecimal`, `id` e modelo sem impressão
- [x] v1.2 — Persistência com SQLite + JDBC
- [x] v1.3 — Maven e separação em camadas
- [ ] v1.4 — Testes automatizados com JUnit
- [ ] v2.0 — API REST com Spring Boot
- [ ] v2.1 — Front-end web consumindo a API

---

Projeto iniciado em Julho de 2026.
