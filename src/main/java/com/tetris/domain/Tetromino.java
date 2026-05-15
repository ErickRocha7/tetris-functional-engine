package com.tetris.domain;

import java.util.List;

public record Tetromino(Shape shape, MatrixPosition position, Orientation orientation) {

    public enum Shape {
        I, J, L, O, S, T, Z
    }

    public enum Orientation {
        NORTH, EAST, SOUTH, WEST;

        public Orientation rotateClockwise() {
            return switch (this) {
                case NORTH -> EAST;
                case EAST -> SOUTH;
                case SOUTH -> WEST;
                case WEST -> NORTH;
            };
        }
    }

    /**
     * Retorna uma nova instância deslocada lateralmente ou verticalmente.
     */
    public Tetromino move(int deltaRow, int deltaCol) {
        return new Tetromino(this.shape, this.position.add(deltaRow, deltaCol), this.orientation);
    }

    /**
     * Retorna uma nova instância rotacionada em sentido horário.
     */
    public Tetromino rotate() {
        return new Tetromino(this.shape, this.position, this.orientation.rotateClockwise());
    }

    /**
     * Calcula dinamicamente as posições absolutas ocupadas pelos blocos da peça na
     * matriz
     * com base no seu formato (Shape) e orientação atual.
     */
    public List<MatrixPosition> currentCells() {
        // Matriz de deslocamentos relativos locais [row, col] para cada formato e
        // rotação
        int[][] offsets = switch (this.shape) {
            case O -> new int[][] { { 0, 0 }, { 0, 1 }, { 1, 0 }, { 1, 1 } }; // Formato O não muda com rotação
            case I -> switch (this.orientation) {
                case NORTH, SOUTH -> new int[][] { { 0, -1 }, { 0, 0 }, { 0, 1 }, { 0, 2 } };
                case EAST, WEST -> new int[][] { { -1, 0 }, { 0, 0 }, { 1, 0 }, { 2, 0 } };
            };
            case T -> switch (this.orientation) {
                case NORTH -> new int[][] { { 0, -1 }, { 0, 0 }, { 0, 1 }, { -1, 0 } };
                case EAST -> new int[][] { { -1, 0 }, { 0, 0 }, { 1, 0 }, { 0, 1 } };
                case SOUTH -> new int[][] { { 0, -1 }, { 0, 0 }, { 0, 1 }, { 1, 0 } };
                case WEST -> new int[][] { { -1, 0 }, { 0, 0 }, { 1, 0 }, { 0, -1 } };
            };
            // Simplificação para os demais formatos (J, L, S, Z) mantendo 4 blocos padrão
            default -> new int[][] { { 0, -1 }, { 0, 0 }, { 0, 1 }, { 1, 0 } };
        };

        return List.of(
                this.position.add(offsets[0][0], offsets[0][1]),
                this.position.add(offsets[1][0], offsets[1][1]),
                this.position.add(offsets[2][0], offsets[2][1]),
                this.position.add(offsets[3][0], offsets[3][1]));
    }
}
