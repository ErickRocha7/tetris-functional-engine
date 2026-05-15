package com.tetris.domain;

import java.util.Arrays;

/**
 * Representação puramente imutável do tabuleiro do Tetris.
 */
public record Board(Tetromino.Shape[][] matrix) {

    private static final int ROWS = 20;
    private static final int COLS = 10;

    /**
     * Cria uma instância inicial com um tabuleiro completamente vazio.
     */
    public static Board createEmpty() {
        return new Board(new Tetromino.Shape[ROWS][COLS]);
    }

    /**
     * Valida de forma puramente determinística se uma peça está em uma posição
     * legal,
     * sem ultrapassar os limites ou colidir com blocos fixados existentes.
     */
    public boolean isValidPosition(Tetromino piece) {
        for (MatrixPosition cell : piece.currentCells()) {
            // Verifica limites laterais e inferiores do tabuleiro
            if (cell.col() < 0 || cell.col() >= COLS || cell.row() >= ROWS) {
                return false;
            }
            // Peças acima do topo da tela (row < 0) são toleradas se não estourarem as
            // laterais
            if (cell.row() >= 0) {
                // Verifica colisão com blocos já travados na matriz estática
                if (matrix[cell.row()][cell.col()] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Grava definitivamente os blocos de um Tetromino no tabuleiro,
     * retornando uma nova cópia imutável com a peça fundida à matriz.
     */
    public Board mergePiece(Tetromino piece) {
        Tetromino.Shape[][] newMatrix = new Tetromino.Shape[ROWS][];
        for (int r = 0; r < ROWS; r++) {
            newMatrix[r] = Arrays.copyOf(matrix[r], COLS);
        }

        for (MatrixPosition cell : piece.currentCells()) {
            if (cell.row() >= 0 && cell.row() < ROWS && cell.col() >= 0 && cell.col() < COLS) {
                newMatrix[cell.row()][cell.col()] = piece.shape();
            }
        }
        return new Board(newMatrix);
    }
}
