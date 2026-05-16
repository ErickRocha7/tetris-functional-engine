package com.tetris.domain;

import java.util.List;

public record GameState(
        Board board,
        Tetromino activePiece,
        List<Tetromino.Shape> nextQueue,
        long score,
        long linesCleared,
        GameStatus status,
        long seed // <-- Acoplamento matemático da semente ao snapshot temporal
) {
    public static GameState createInitial(List<Tetromino.Shape> initialQueue, Tetromino firstPiece, long initialSeed) {
        return new GameState(
                Board.createEmpty(),
                firstPiece,
                initialQueue,
                0L,
                0L,
                GameStatus.PLAYING,
                initialSeed);
    }

    public GameState withStatus(GameStatus newStatus) {
        return new GameState(this.board, this.activePiece, this.nextQueue, this.score, this.linesCleared, newStatus,
                this.seed);
    }
}
