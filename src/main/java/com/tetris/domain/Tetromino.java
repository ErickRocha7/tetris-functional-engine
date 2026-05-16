package com.tetris.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

        public Orientation rotateCounterClockwise() {
            return switch (this) {
                case NORTH -> WEST;
                case WEST -> SOUTH;
                case SOUTH -> EAST;
                case EAST -> NORTH;
            };
        }
    }

    // ================================================================
    // Definição imutável das células para cada (Shape, Orientation)
    // ================================================================
    private static final Map<Shape, Map<Orientation, List<MatrixPosition>>> CELLS_MAP = Map.of(
            Shape.I, Map.of(
                    Orientation.NORTH, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(1, 1),
                            new MatrixPosition(2, 1), new MatrixPosition(3, 1)),
                    Orientation.EAST, List.of(
                            new MatrixPosition(1, 0), new MatrixPosition(1, 1),
                            new MatrixPosition(1, 2), new MatrixPosition(1, 3)),
                    Orientation.SOUTH, List.of(
                            new MatrixPosition(0, 2), new MatrixPosition(1, 2),
                            new MatrixPosition(2, 2), new MatrixPosition(3, 2)),
                    Orientation.WEST, List.of(
                            new MatrixPosition(2, 0), new MatrixPosition(2, 1),
                            new MatrixPosition(2, 2), new MatrixPosition(2, 3))),
            Shape.J, Map.of(
                    Orientation.NORTH, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(1, 1),
                            new MatrixPosition(2, 0), new MatrixPosition(2, 1)),
                    Orientation.EAST, List.of(
                            new MatrixPosition(1, 0), new MatrixPosition(1, 1),
                            new MatrixPosition(1, 2), new MatrixPosition(2, 2)),
                    Orientation.SOUTH, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(0, 2),
                            new MatrixPosition(1, 1), new MatrixPosition(2, 1)),
                    Orientation.WEST, List.of(
                            new MatrixPosition(0, 0), new MatrixPosition(1, 0),
                            new MatrixPosition(1, 1), new MatrixPosition(1, 2))),
            Shape.L, Map.of(
                    Orientation.NORTH, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(1, 1),
                            new MatrixPosition(2, 1), new MatrixPosition(2, 2)),
                    Orientation.EAST, List.of(
                            new MatrixPosition(1, 0), new MatrixPosition(1, 1),
                            new MatrixPosition(1, 2), new MatrixPosition(2, 0)),
                    Orientation.SOUTH, List.of(
                            new MatrixPosition(0, 0), new MatrixPosition(0, 1),
                            new MatrixPosition(1, 1), new MatrixPosition(2, 1)),
                    Orientation.WEST, List.of(
                            new MatrixPosition(0, 2), new MatrixPosition(1, 0),
                            new MatrixPosition(1, 1), new MatrixPosition(1, 2))),
            Shape.O, Map.of(
                    Orientation.NORTH, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(0, 2),
                            new MatrixPosition(1, 1), new MatrixPosition(1, 2)),
                    Orientation.EAST, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(0, 2),
                            new MatrixPosition(1, 1), new MatrixPosition(1, 2)),
                    Orientation.SOUTH, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(0, 2),
                            new MatrixPosition(1, 1), new MatrixPosition(1, 2)),
                    Orientation.WEST, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(0, 2),
                            new MatrixPosition(1, 1), new MatrixPosition(1, 2))),
            // ============================================================
            // PEÇA S – com EAST e WEST corrigidos (não mais quadrado 2x2)
            // ============================================================
            Shape.S, Map.of(
                    Orientation.NORTH, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(0, 2),
                            new MatrixPosition(1, 0), new MatrixPosition(1, 1)),
                    Orientation.EAST, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(1, 1),
                            new MatrixPosition(1, 2), new MatrixPosition(2, 2)),
                    Orientation.SOUTH, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(0, 2),
                            new MatrixPosition(1, 0), new MatrixPosition(1, 1)),
                    Orientation.WEST, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(1, 1),
                            new MatrixPosition(1, 2), new MatrixPosition(2, 2))),
            Shape.T, Map.of(
                    Orientation.NORTH, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(1, 0),
                            new MatrixPosition(1, 1), new MatrixPosition(1, 2)),
                    Orientation.EAST, List.of(
                            new MatrixPosition(1, 1), new MatrixPosition(0, 1),
                            new MatrixPosition(1, 0), new MatrixPosition(2, 1)),
                    Orientation.SOUTH, List.of(
                            new MatrixPosition(1, 0), new MatrixPosition(1, 1),
                            new MatrixPosition(1, 2), new MatrixPosition(2, 1)),
                    Orientation.WEST, List.of(
                            new MatrixPosition(0, 1), new MatrixPosition(1, 1),
                            new MatrixPosition(1, 2), new MatrixPosition(2, 1))),
            // ============================================================
            // PEÇA Z – com EAST e WEST corrigidos
            // ============================================================
            Shape.Z, Map.of(
                    Orientation.NORTH, List.of(
                            new MatrixPosition(0, 0), new MatrixPosition(0, 1),
                            new MatrixPosition(1, 1), new MatrixPosition(1, 2)),
                    Orientation.EAST, List.of(
                            new MatrixPosition(0, 2), new MatrixPosition(1, 1),
                            new MatrixPosition(1, 2), new MatrixPosition(2, 1)),
                    Orientation.SOUTH, List.of(
                            new MatrixPosition(0, 0), new MatrixPosition(0, 1),
                            new MatrixPosition(1, 1), new MatrixPosition(1, 2)),
                    Orientation.WEST, List.of(
                            new MatrixPosition(0, 2), new MatrixPosition(1, 1),
                            new MatrixPosition(1, 2), new MatrixPosition(2, 1))));

    public Tetromino move(int deltaRow, int deltaCol) {
        return new Tetromino(this.shape, this.position.add(deltaRow, deltaCol), this.orientation);
    }

    public Tetromino rotate() {
        return new Tetromino(this.shape, this.position, this.orientation.rotateClockwise());
    }

    public List<MatrixPosition> currentCells() {
        Map<Orientation, List<MatrixPosition>> shapeMap = CELLS_MAP.get(shape);
        if (shapeMap == null)
            return List.of();
        List<MatrixPosition> relative = shapeMap.getOrDefault(orientation, List.of());
        List<MatrixPosition> absolute = new ArrayList<>(relative.size());
        for (MatrixPosition rel : relative) {
            absolute.add(position.add(rel.row(), rel.col()));
        }
        return Collections.unmodifiableList(absolute);
    }
}