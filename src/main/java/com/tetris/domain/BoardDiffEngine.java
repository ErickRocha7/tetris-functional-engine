package com.tetris.domain;

import java.util.ArrayList;
import java.util.List;

public final class BoardDiffEngine {

    /**
     * Representação de uma alteração pontual em uma coordenada da matriz.
     */
    public record CellPatch(int row, int col, Tetromino.Shape shape) {
    }

    /**
     * Compara duas matrizes de tabuleiro e extrai apenas as coordenadas que
     * sofreram modificação.
     */
    public static List<CellPatch> computeDiff(Board oldBoard, Board newBoard) {
        List<CellPatch> patches = new ArrayList<>();
        Tetromino.Shape[][] oldMat = oldBoard.matrix();
        Tetromino.Shape[][] newMat = newBoard.matrix();

        for (int r = 0; r < oldMat.length; r++) {
            for (int c = 0; c < oldMat[r].length; c++) {
                if (oldMat[r][c] != newMat[r][c]) {
                    patches.add(new CellPatch(r, c, newMat[r][c]));
                }
            }
        }
        return List.copyOf(patches);
    }
}
