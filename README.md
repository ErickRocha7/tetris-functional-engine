# Functional Tetris Engine 🚀

Uma mini-engine reativa de Tetris construída em Java 21 com foco em arquitetura funcional, concorrência segura por confinamento, determinismo absoluto e renderização incremental.

Este projeto foi concebido estritamente como um **laboratório arquitetural de sistemas concorrentes e orientados a eventos**, utilizando princípios de *Functional Core / Imperative Shell*, *Elm Architecture* e *MVI*.

## 🛠️ Pilares Arquiteturais

### 1. Modelagem Algébrica e Imutabilidade Estrita (`domain`)
* **Tipos Produto e Soma:** O domínio é modelado via `records` e `sealed interfaces` do Java 21, permitindo exaustividade em tempo de compilação.
* **Função Reducer Pura:** Toda transição de estado ocorre exclusivamente via `GameReducer.reduce(currentState, event) -> newState`. Não existem mutações compartilhadas, efeitos colaterais internos ou dependências temporais ocultas.
* **Física Protetiva Preditiva:** O `PhysicsEngine` simula e valida movimentos, rotações e *wall kicks* antes de gerar um novo estado. Estados inválidos nunca são produzidos.
* **Aleatoriedade Determinística (7-Bag Real):** O gerador pseudoaleatório baseia-se em uma semente imutável rastreada dentro do `GameState`. A sequência de peças gerada é idêntica para a mesma semente, garantindo reprodutibilidade.

### 2. Concorrência Segura por Isolamento (`application`)
* **Thread Confinement:** Toda modificação de estado ocorre em uma única thread dedicada (`tetris-engine-thread`), eliminando *race conditions* por design. Dispensa o uso de `synchronized` ou travas imperativas pesadas.
* **Fila Sequencial Discreta:** Entradas do usuário, ticks gravitacionais e animações são serializados através de uma `LinkedBlockingQueue`.
* **O Tempo como Dado:** O `GameScheduler` opera isolado e não move peças; ele apenas injeta eventos `TimeTick` no barramento ordenado.

### 3. Projeção Visual e Renderização Incremental (`infrastructure-ui`)
* **Fluxo Unidirecional de Dados (UDF):** A UI JavaFX atua estritamente como uma camada passiva "burra" (produtora de eventos, observadora de snapshots e renderizadora).
* **Reconciliação por Patches:** Utiliza um `BoardDiffEngine` (inspirado em *Virtual DOM*) para calcular diferenças estruturais entre snapshots consecutivos. A UI renderiza apenas as células modificadas (`CellPatches`), otimizando drasticamente o consumo de CPU.

### 4. Telemetria e Replay Determinístico
* **Modo Cinema:** O `ReplayPlayer` reconstrói partidas inteiras milissegundo por milissegundo reinjetando os dados salvos em cima da mesma semente inicial.
* **Auditoria de Performance:** Módulo `MetricsAnalyzer` que avalia os logs e calcula com precisão matemática o **APM (Ações por Minuto)** real do jogador humano.

---

## 📂 Estrutura do Projeto

```text
tetris-functional-engine/
├── src/
│   ├── main/java/com/tetris/
│   │   ├── domain/              <-- Core Funcional Puro (Sem Efeitos Colaterais)
│   │   ├── application/         <-- Imperative Shell (Fila Concorrente e Agendador)
│   │   └── infrastructure/ui/   <-- Camada Periférica (JavaFX Canvas e Tradução de Input)
│   └── test/java/com/tetris/    <-- Testes Unitários de Pureza e Imutabilidade (JUnit 5)
├── pom.xml
└── README.md
```

---

## ⚙️ Como Executar e Testar

### Pré-requisitos
* **Java 21** (JDK instalado)
* **Apache Maven** configurado no Path do sistema

### Executar a Suite de Testes (JUnit 5)
Para rodar os testes automatizados que validam a imutabilidade e a transparência referencial do Reducer:
```bash
mvn test
```

### Executar o Jogo
Para baixar as dependências de runtime do JavaFX e inicializar a engine:
```bash
mvn javafx:run
```

---
*Desenvolvido sob os princípios de imutabilidade estrita e determinismo matemático.*
