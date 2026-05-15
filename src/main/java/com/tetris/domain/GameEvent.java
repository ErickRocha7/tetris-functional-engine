package com.tetris.domain;

/**
 * Tipo Somatório (Sum Type) que define exaustivamente em tempo de compilação
 * todas as intenções e gatilhos possíveis dentro do ecossistema do Tetris.
 */
public sealed interface GameEvent {

    // Comandos de movimentação lateral iniciados pelo jogador
    record MoveLeft() implements GameEvent {
    }

    record MoveRight() implements GameEvent {
    }

    // Comando de rotação da peça ativa iniciado pelo jogador
    record Rotate() implements GameEvent {
    }

    // Queda instantânea da peça ativa até o ponto de colisão inferior
    record HardDrop() implements GameEvent {
    }

    // Evento temporal discreto disparado pelo relógio físico da engine (Gravidade)
    record TimeTick() implements GameEvent {
    }

    // Evento de ciclo de vida indicando a conclusão de uma animação visual na UI
    record LineAnimationEnd() implements GameEvent {
    }
}
