package com.tetris.domain;

public final class PhysicsEngine {

    /**
     * Tenta deslocar a peça.
     */
    public static Tetromino tryMove(Board board, Tetromino current, int deltaRow, int deltaCol) {
        Tetromino projected = current.move(deltaRow, deltaCol);
        return board.isValidPosition(projected) ? projected : current;
    }

    /**
     * Tenta rotacionar a peça aplicando uma lógica de Wall Kick simplificada.
     * Testa a rotação pura e, se falhar, tenta pequenos deslocamentos.
     * Esta abordagem resolve a maioria dos casos de rotação colada em paredes ou
     * outras peças.
     */
    public static Tetromino tryRotate(Board board, Tetromino current) {
        Tetromino rotated = current.rotate();

        // Lista de deslocamentos (deltaRow, deltaCol) a serem testados após a rotação
        int[][] kicks = {
                { 0, 0 }, // rotação pura
                { 0, -1 }, // chutar esquerda
                { 0, 1 }, // chutar direita
                { -1, 0 }, // chutar para cima (útil em colisão com peças abaixo)
                { 1, 0 } // chutar para baixo (raro, mas ajuda em bordas)
        };

        for (int[] kick : kicks) {
            Tetromino kicked = new Tetromino(
                    rotated.shape(),
                    rotated.position().add(kick[0], kick[1]),
                    rotated.orientation());
            if (board.isValidPosition(kicked)) {
                return kicked;
            }
        }

        // Se nenhum kick funcionar, a rotação é impossível
        return current;
    }
}