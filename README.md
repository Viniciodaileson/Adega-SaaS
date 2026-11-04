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
- SQLite (via JDBC)
- Git
- GitHub

## Funcionalidades (v1.2)

- Cadastro de bebidas com `id`, nome, preço e quantidade em estoque
- Consulta do estoque com valor total
- Reposição de estoque
- Venda com baixa automática no estoque
- Validação de dados (nome, preço, quantidade e entradas inválidas)
- Bloqueio de bebidas com nome duplicado (na aplicação e no banco)
- Preço com `BigDecimal` (sem erro de arredondamento em dinheiro)
- **Dados salvos em SQLite: nada se perde ao fechar o programa**

## Como executar

Pré-requisitos: JDK 21 (`java -version`) e o driver JDBC do SQLite.

```powershell
# 1. Baixa o driver para lib/ (só precisa na primeira vez)
.\baixar-driver.ps1

# 2. Compila
javac -cp "lib\sqlite-jdbc-3.54.1.0.jar" *.java

# 3. Executa
java -cp ".;lib\sqlite-jdbc-3.54.1.0.jar" CadastroBebidas
```

Se o Windows bloquear o script, use:

```powershell
powershell -ExecutionPolicy Bypass -File .\baixar-driver.ps1
```

No VS Code, com a extensão **Extension Pack for Java** instalada, o `.vscode/settings.json` já aponta para o driver — basta usar o botão ▶.

## Estrutura

```
AdegaSaaS/
├── Bebida.java            # Modelo: dados, identidade e regras de estoque
├── Resultado.java         # Retorno das operações (sucesso/falha + mensagem)
├── ConexaoBanco.java      # Abre a conexão e cria a tabela
├── BebidaRepository.java  # SQL: salvar, listar, buscar e atualizar
├── CadastroBebidas.java   # Interface de console (menu)
├── baixar-driver.ps1      # Baixa o driver JDBC do SQLite
├── lib/                   # Driver (não versionado)
└── adega.db               # Banco de dados (não versionado)
```

## Banco de dados

O arquivo `adega.db` é criado automaticamente na primeira execução, nesta pasta.

```sql
CREATE TABLE bebidas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL COLLATE NOCASE UNIQUE,
    preco TEXT NOT NULL,
    quant_estoque INTEGER NOT NULL CHECK (quant_estoque >= 0)
);
```

- `AUTOINCREMENT`: o banco gera o `id`, a aplicação não inventa mais números.
- `COLLATE NOCASE UNIQUE`: impede nomes repetidos ignorando maiúsculas/minúsculas.
- `preco TEXT`: o SQLite não tem tipo decimal; guardamos como texto para não perder centavos. (Em produção, o comum é guardar centavos em `INTEGER`.)
- `CHECK (quant_estoque >= 0)`: o banco também recusa estoque negativo.

Para começar do zero, apague `adega.db` e rode de novo.

## Roadmap

- [x] v0.1 — Cadastro e venda inicial
- [x] v0.2 — Refatoração para orientação a objetos
- [x] v1.0 — Menu completo, validações e encapsulamento
- [x] v1.1 — `BigDecimal`, `id` e modelo sem impressão
- [x] v1.2 — Persistência com SQLite + JDBC
- [ ] v1.3 — Separação em camadas (model, repository, service, ui)
- [ ] v2.0 — API REST com Spring Boot

---

Projeto iniciado em Julho de 2026.
