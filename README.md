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
- Git
- GitHub

## Funcionalidades (v1.0)

- Cadastro de bebidas com nome, preço e quantidade em estoque
- Consulta do estoque com valor total
- Reposição de estoque
- Venda com baixa automática no estoque
- Validação de dados (nome, preço, quantidade e entradas inválidas)
- Bloqueio de bebidas com nome duplicado

## Como executar

Pré-requisito: JDK 21 instalado (`java -version`).

```bash
javac Bebida.java CadastroBebidas.java
java CadastroBebidas
```

## Estrutura

```
AdegaSaaS/
├── Bebida.java            # Modelo: dados e regras de estoque
├── CadastroBebidas.java   # Interface de console (menu)
├── .gitignore
└── README.md
```

## Roadmap

- [x] v0.1 — Cadastro e venda inicial
- [x] v0.2 — Refatoração para orientação a objetos
- [x] v1.0 — Menu completo, validações e encapsulamento
- [ ] v1.1 — Persistência com SQLite + JDBC
- [ ] v1.2 — Separação em camadas (model, repository, service, ui)
- [ ] v2.0 — API REST com Spring Boot

---

Projeto iniciado em Julho de 2026.
