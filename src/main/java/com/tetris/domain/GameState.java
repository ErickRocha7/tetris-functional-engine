package com.tetris.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Snapshot imutável completo do estado do jogo.
 * Representa um ponto no tempo da simulação determinística.
 * Agora armazena informações de linhas pendentes de limpeza.
 * 
 * A criação do estado inicial é puramente baseada em uma seed,
 * garantindo que toda a aleatoriedade fique encapsulada no domínio.
 */
public record GameState(
        Board board,
        Tetromino activePiece,
        List<Tetromino.Shape> nextQueue,
        long score,
        long linesCleared,
        GameStatus status,
        long seed,
        Optional<GameReducer.LineClearInfo> pendingClear) {

    /**
     * Cria o estado inicial de forma puramente determinística a partir de uma seed.
     * Nenhuma peça ou bag é fornecida externamente – o domínio gera tudo.
     */
    public static GameState createInitial(long initialSeed) {
        // Gera o primeiro bag e a primeira peça dentro do domínio
        var firstBag = DeterministicGenerator.generateBag(initialSeed);
        List<Tetromino.Shape> queue = new ArrayList<>(firstBag.bag());
        Tetromino.Shape firstShape = queue.remove(0);
        Tetromino firstPiece = new Tetromino(
                firstShape,
                new MatrixPosition(0, 4),
                Tetromino.Orientation.NORTH);
        // A próxima seed é a que será usada para o próximo bag (quando necessário)
        long nextSeed = firstBag.nextSeed();

        return new GameState(
                Board.createEmpty(),
                firstPiece,
                List.copyOf(queue),
                0L,
                0L,
                GameStatus.PLAYING,
                nextSeed, // guarda a seed para o próximo bag
                Optional.empty());
    }

    /**
     * Retorna um novo estado com status alterado (transição funcional).
     */
    public GameState withStatus(GameStatus newStatus) {
        return new GameState(
                this.board,
                this.activePiece,
                this.nextQueue,
                this.score,
                this.linesCleared,
                newStatus,
                this.seed,
                this.pendingClear);
    }

    /**
     * Retorna um novo estado com novo tabuleiro.
     */
    public GameState withBoard(Board newBoard) {
        return new GameState(
                newBoard,
                this.activePiece,
                this.nextQueue,
                this.score,
                this.linesCleared,
                this.status,
                this.seed,
                this.pendingClear);
    }

    /**
     * Retorna um novo estado com score atualizado.
     */
    public GameState withScore(long newScore) {
        return new GameState(
                this.board,
                this.activePiece,
                this.nextQueue,
                newScore,
                this.linesCleared,
                this.status,
                this.seed,
                this.pendingClear);
    }

    /**
     * Retorna um novo estado com informações de linhas pendentes.
     */
    public GameState withPendingClear(GameReducer.LineClearInfo clearInfo) {
        return new GameState(
                this.board,
                this.activePiece,
                this.nextQueue,
                this.score,
                this.linesCleared,
                this.status,
                this.seed,
                Optional.of(clearInfo));
    }

    /**
     * Retorna um novo estado após a limpeza das linhas.
     */
    public GameState withClearedBoard(Board newBoard, long scoreIncrement, long linesIncrement) {
        return new GameState(
                newBoard,
                this.activePiece,
                this.nextQueue,
                this.score + scoreIncrement,
                this.linesCleared + linesIncrement,
                GameStatus.PLAYING,
                this.seed,
                Optional.empty());
    }
}