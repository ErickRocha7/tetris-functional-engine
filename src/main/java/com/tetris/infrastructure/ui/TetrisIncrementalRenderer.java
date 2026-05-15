package com.tetris.infrastructure.ui;

import com.tetris.domain.Board;
import com.tetris.domain.BoardDiffEngine;
import com.tetris.domain.GameState;
import com.tetris.domain.GameStatus;
import com.tetris.domain.MatrixPosition;
import com.tetris.domain.Tetromino;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Renderizador Incremental e Passivo (Output Shell).
 * Utiliza o BoardDiffEngine para aplicar patches visuais mínimos diretamente no
 * Canvas.
 */
public final class TetrisIncrementalRenderer {

    private static final int CELL_SIZE = 30;
    private final Canvas canvas;
    private final GraphicsContext gc;

    // Cache do último estado estável do tabuleiro para cálculo diferencial de
    // patches
    private Board lastRenderedBoard;
    // Armazena as últimas células ocupadas pela peça ativa para limpá-las
    // seletivamente
    private List<MatrixPosition> lastActiveCells;

    public TetrisIncrementalRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.lastRenderedBoard = Board.createEmpty();
        this.lastActiveCells = new ArrayList<>();

        // Inicializa o background preto de forma estática uma única vez
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Projeta incrementalmente o snapshot de estado na tela.
     * Deve ser invocado obrigatoriamente a partir da JavaFX Application Thread.
     */
    public void render(GameState state) {
        // 1. Limpa seletivamente e apenas as posições antigas da peça ativa do frame
        // anterior
        gc.setFill(Color.BLACK);
        for (MatrixPosition pos : lastActiveCells) {
            // Garante que não vamos apagar um bloco fixado do tabuleiro que esteja por
            // baixo
            if (pos.row() >= 0 && pos.row() < 20 && pos.col() >= 0 && pos.col() < 10) {
                Tetromino.Shape boardShape = state.board().matrix()[pos.row()][pos.col()];
                if (boardShape == null) {
                    gc.fillRect(pos.col() * CELL_SIZE, pos.row() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                } else {
                    drawCell(pos.col(), pos.row(), boardShape);
                }
            }
        }

        // 2. Reconciliação Incremental: Calcula e aplica patches apenas nas células
        // modificadas do tabuleiro
        List<BoardDiffEngine.CellPatch> patches = BoardDiffEngine.computeDiff(this.lastRenderedBoard, state.board());
        for (BoardDiffEngine.CellPatch patch : patches) {
            if (patch.shape() == null) {
                gc.setFill(Color.BLACK);
                gc.fillRect(patch.col() * CELL_SIZE, patch.row() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            } else {
                drawCell(patch.col(), patch.row(), patch.shape());
            }
        }

        // Atualiza o cache do tabuleiro estável
        this.lastRenderedBoard = state.board();

        // 3. Projeta e renderiza a nova configuração espacial da peça ativa
        List<MatrixPosition> currentActiveCells = new ArrayList<>();
        if (state.status() == GameStatus.PLAYING && state.activePiece() != null) {
            for (MatrixPosition pos : state.activePiece().currentCells()) {
                if (pos.row() >= 0) {
                    drawCell(pos.col(), pos.row(), state.activePiece().shape());
                    currentActiveCells.add(pos);
                }
            }
        }

        // Armazena a posição da peça para a limpeza incremental do próximo frame
        this.lastActiveCells = currentActiveCells;

        // 4. Overlays declarativos de controle da máquina de estados
        if (state.status() == GameStatus.GAME_OVER) {
            drawGameOverOverlay();
        }
    }

    /**
     * Desenha um bloco estilizado de Tetris com base no seu tipo algébrico.
     */
    private void drawCell(int x, int y, Tetromino.Shape shape) {
        Color color = switch (shape) {
            case I -> Color.CYAN;
            case J -> Color.BLUE;
            case L -> Color.ORANGE;
            case O -> Color.YELLOW;
            case S -> Color.GREEN;
            case T -> Color.PURPLE;
            case Z -> Color.RED;
        };

        // Preenche o corpo do bloco
        gc.setFill(color);
        gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

        // Renderiza uma borda sutil para separação visual dos blocos individuais
        gc.setStroke(color.darker());
        gc.setLineWidth(1);
        gc.strokeRect(x * CELL_SIZE + 0.5, y * CELL_SIZE + 0.5, CELL_SIZE - 1, CELL_SIZE - 1);
    }

    /**
     * Desenha uma sobreposição estática textual caso o estado mude para GAME_OVER.
     */
    private void drawGameOverOverlay() {
        gc.setFill(new Color(0, 0, 0, 0.75)); // Overlay semitransparente
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", 24));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("GAME OVER", canvas.getWidth() / 2, canvas.getHeight() / 2);
    }
}
