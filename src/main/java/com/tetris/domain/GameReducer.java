package com.tetris.domain;

import java.util.ArrayList;
import java.util.List;

public final class GameReducer {

    /**
     * Função matemática pura central:
     * NovoEstado = reduce(EstadoAtual, Evento)
     */
    public static GameState reduce(GameState current, GameEvent event) {

        // Bloqueio declarativo se o jogo estiver encerrado
        if (current.status() == GameStatus.GAME_OVER) {
            return current;
        }

        // Bloqueio declarativo durante animações
        if (current.status() == GameStatus.ANIMATING_LINES
                && !(event instanceof GameEvent.LineAnimationEnd)) {

            return current;
        }

        return switch (event) {

            case GameEvent.MoveLeft() ->
                handleMove(current, 0, -1);

            case GameEvent.MoveRight() ->
                handleMove(current, 0, 1);

            case GameEvent.Rotate() ->
                handleRotation(current);

            case GameEvent.TimeTick() ->
                handleGravity(current);

            case GameEvent.HardDrop() ->
                handleHardDrop(current);

            case GameEvent.LineAnimationEnd() ->
                completeLineClearing(current);
        };
    }

    private static GameState handleMove(
            GameState state,
            int dRow,
            int dCol) {

        Tetromino moved = PhysicsEngine.tryMove(
                state.board(),
                state.activePiece(),
                dRow,
                dCol);

        if (moved == state.activePiece()) {
            return state;
        }

        return new GameState(
                state.board(),
                moved,
                state.nextQueue(),
                state.score(),
                state.linesCleared(),
                state.status(),
                state.seed());
    }

    private static GameState handleRotation(GameState state) {

        Tetromino rotated = PhysicsEngine.tryRotate(
                state.board(),
                state.activePiece());

        if (rotated == state.activePiece()) {
            return state;
        }

        return new GameState(
                state.board(),
                rotated,
                state.nextQueue(),
                state.score(),
                state.linesCleared(),
                state.status(),
                state.seed());
    }

    private static GameState handleGravity(GameState state) {

        Tetromino currentPiece = state.activePiece();

        Tetromino movedDown = currentPiece.move(1, 0);

        // Gravidade natural
        if (state.board().isValidPosition(movedDown)) {

            return new GameState(
                    state.board(),
                    movedDown,
                    state.nextQueue(),
                    state.score(),
                    state.linesCleared(),
                    state.status(),
                    state.seed());
        }

        // =========================================================
        // Lock-in da peça no tabuleiro
        // =========================================================

        Board mergedBoard = state.board().mergePiece(currentPiece);

        boolean hasLinesToClear = checkFullLines(mergedBoard);

        GameStatus nextStatus = hasLinesToClear
                ? GameStatus.ANIMATING_LINES
                : GameStatus.PLAYING;

        // =========================================================
        // Condição de Game Over
        // =========================================================

        if (!hasLinesToClear && isGameOverCondition(mergedBoard)) {

            return state.withStatus(GameStatus.GAME_OVER);
        }

        // =========================================================
        // Pipeline determinístico da fila de peças
        // =========================================================

        List<Tetromino.Shape> updatedQueue = new ArrayList<>(state.nextQueue());

        Tetromino.Shape nextShape = updatedQueue.isEmpty()
                ? Tetromino.Shape.I
                : updatedQueue.remove(0);

        long currentSeed = state.seed();

        // Se a fila estiver abaixo do limite mínimo,
        // gera um novo 7-Bag determinístico
        if (updatedQueue.size() < 7) {

            DeterministicGenerator.GeneratorResult result = DeterministicGenerator.generateBag(currentSeed);

            updatedQueue.addAll(result.bag());

            // Avança matematicamente a semente
            currentSeed = result.nextSeed();
        }

        // =========================================================
        // Spawn da nova peça
        // =========================================================

        Tetromino newActivePiece = new Tetromino(
                nextShape,
                new MatrixPosition(0, 4),
                Tetromino.Orientation.NORTH);

        return new GameState(
                mergedBoard,
                newActivePiece,
                List.copyOf(updatedQueue),
                state.score(),
                state.linesCleared(),
                nextStatus,
                currentSeed);
    }

    private static GameState handleHardDrop(GameState state) {

        GameState step = state;

        Tetromino current = step.activePiece();

        while (step.board().isValidPosition(current.move(1, 0))) {

            current = current.move(1, 0);

            step = new GameState(
                    step.board(),
                    current,
                    step.nextQueue(),
                    step.score() + 2,
                    step.linesCleared(),
                    step.status(),
                    step.seed());
        }

        return handleGravity(step);
    }

    private static GameState completeLineClearing(GameState state) {

        Tetromino.Shape[][] originalMat = state.board().matrix();

        List<Tetromino.Shape[]> newMatrixList = new ArrayList<>();

        for (Tetromino.Shape[] row : originalMat) {

            boolean isFull = true;

            for (Tetromino.Shape cell : row) {

                if (cell == null) {
                    isFull = false;
                    break;
                }
            }

            if (!isFull) {
                newMatrixList.add(row);
            }
        }

        int clearedCount = originalMat.length - newMatrixList.size();

        while (newMatrixList.size() < originalMat.length) {

            newMatrixList.add(
                    0,
                    new Tetromino.Shape[originalMat[0].length]);
        }

        long newScore = state.score() + (clearedCount * 100L);

        long newLines = state.linesCleared() + clearedCount;

        Board finalBoard = new Board(
                newMatrixList.toArray(new Tetromino.Shape[0][0]));

        return new GameState(
                finalBoard,
                state.activePiece(),
                state.nextQueue(),
                newScore,
                newLines,
                GameStatus.PLAYING,
                state.seed());
    }

    private static boolean checkFullLines(Board board) {

        for (Tetromino.Shape[] row : board.matrix()) {

            boolean isFull = true;

            for (Tetromino.Shape cell : row) {

                if (cell == null) {
                    isFull = false;
                    break;
                }
            }

            if (isFull) {
                return true;
            }
        }

        return false;
    }

    private static boolean isGameOverCondition(Board board) {

        for (int c = 3; c <= 6; c++) {

            if (board.matrix()[0][c] != null) {
                return true;
            }
        }

        return false;
    }
}