package com.tetris.domain;

import java.util.List;

/**
 * Snapshot integral e 100% imutável de todo o estado da engine do jogo.
 * Nenhuma alteração ocorre por mutação; qualquer mudança gera uma nova
 * instância de GameState.
 */
public record GameState(
        Board board,
        Tetromino activePiece,
        List<Tetromino.Shape> nextQueue,
        long score,
        long linesCleared,
        GameStatus status) {

    /**
     * Instancia o estado inicial padrão para o início de uma nova partida.
     */
    public static GameState createInitial(List<Tetromino.Shape> initialQueue, Tetromino firstPiece) {
        return new GameState(
                Board.createEmpty(),
                firstPiece,
                initialQueue,
                0L, // Pontuação zerada
                0L, // Linhas zeradas
                GameStatus.PLAYING);
    }

    /**
     * Cria uma cópia derivada modificando apenas o status operacional da engine.
     */
    public GameState withStatus(GameStatus newStatus) {
        return new GameState(this.board, this.activePiece, this.nextQueue, this.score, this.linesCleared, newStatus);
    }

    /**
     * Cria uma cópia derivada com uma nova peça ativa e uma fila de próximos blocos
     * atualizada.
     */
    public GameState withNextPiece(Tetromino newActive, List<Tetromino.Shape> updatedQueue) {
        return new GameState(this.board, newActive, updatedQueue, this.score, this.linesCleared, this.status);
    }
}
