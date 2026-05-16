package com.tetris.domain;

import java.util.Arrays;

public final class Board {

    private static final int ROWS = 20;
    private static final int COLS = 10;

    private final Tetromino.Shape[][] matrix;

    private Board(Tetromino.Shape[][] matrix) {
        this.matrix = deepCopy(matrix);
    }

    public static Board createEmpty() {
        return new Board(new Tetromino.Shape[ROWS][COLS]);
    }

    public static Board fromMatrix(Tetromino.Shape[][] source) {
        if (source == null || source.length != ROWS) {
            throw new IllegalArgumentException("Matriz deve ter " + ROWS + " linhas");
        }
        for (Tetromino.Shape[] row : source) {
            if (row == null || row.length != COLS) {
                throw new IllegalArgumentException("Cada linha deve ter " + COLS + " colunas");
            }
        }
        return new Board(source);
    }

    public static int rows() {
        return ROWS;
    }

    public static int cols() {
        return COLS;
    }

    public Tetromino.Shape[][] matrix() {
        return deepCopy(this.matrix);
    }

    public boolean isValidPosition(Tetromino piece) {
        for (MatrixPosition cell : piece.currentCells()) {
            int r = cell.row();
            int c = cell.col();
            if (c < 0 || c >= COLS || r >= ROWS)
                return false;
            if (r >= 0 && matrix[r][c] != null)
                return false;
        }
        return true;
    }

    public Board mergePiece(Tetromino piece) {
        Tetromino.Shape[][] newMatrix = deepCopy(this.matrix);
        for (MatrixPosition cell : piece.currentCells()) {
            int r = cell.row();
            int c = cell.col();
            if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
                newMatrix[r][c] = piece.shape();
            }
        }
        return new Board(newMatrix);
    }

    Tetromino.Shape get(int row, int col) {
        return matrix[row][col];
    }

    private static Tetromino.Shape[][] deepCopy(Tetromino.Shape[][] source) {
        Tetromino.Shape[][] copy = new Tetromino.Shape[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            if (source[r] != null) {
                copy[r] = Arrays.copyOf(source[r], COLS);
            } else {
                copy[r] = new Tetromino.Shape[COLS];
            }
        }
        return copy;
    }
}