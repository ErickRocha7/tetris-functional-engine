package com.tetris.domain;

/**
 * Representação imutável de uma coordenada na matriz bidimensional do jogo.
 * Linhas (row) crescem de cima para baixo; colunas (col) crescem da esquerda
 * para a direita.
 */
public record MatrixPosition(int row, int col) {

    /**
     * Retorna uma nova posição deslocada a partir dos deltas fornecidos.
     */
    public MatrixPosition add(int deltaRow, int deltaCol) {
        return new MatrixPosition(this.row + deltaRow, this.col + deltaCol);
    }
}
