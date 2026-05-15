package com.tetris.domain;

/**
 * Motor físico puramente preditivo e sem estado (stateless).
 * Simula transformações espaciais antes de aplicá-las ao domínio.
 */
public final class PhysicsEngine {

    /**
     * Tenta deslocar a peça lateral ou verticalmente. Se a projeção for válida,
     * retorna a nova configuração, caso contrário retorna a peça original.
     */
    public static Tetromino tryMove(Board board, Tetromino current, int deltaRow, int deltaCol) {
        Tetromino projected = current.move(deltaRow, deltaCol);
        return board.isValidPosition(projected) ? projected : current;
    }

    /**
     * Tenta rotacionar a peça aplicando uma lógica simplificada de Wall Kick.
     */
    public static Tetromino tryRotate(Board board, Tetromino current) {
        Tetromino rotated = current.rotate();

        // 1. Teste de rotação pura
        if (board.isValidPosition(rotated))
            return rotated;

        // 2. Wall Kick: Tenta chutar 1 bloco para a esquerda
        Tetromino kickLeft = rotated.move(0, -1);
        if (board.isValidPosition(kickLeft))
            return kickLeft;

        // 3. Wall Kick: Tenta chutar 1 bloco para a direita
        Tetromino kickRight = rotated.move(0, 1);
        if (board.isValidPosition(kickRight))
            return kickRight;

        // Se todas as projeções falharem, a rotação é fisicamente obstruída
        return current;
    }
}
