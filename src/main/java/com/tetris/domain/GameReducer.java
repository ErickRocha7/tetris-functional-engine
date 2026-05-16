package com.tetris.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GameReducer {

    public record LineClearInfo(int clearedCount, List<Integer> fullRowIndices) {
        public LineClearInfo {
            fullRowIndices = List.copyOf(fullRowIndices);
        }
    }

    private record LineClearingResult(Board clearedBoard, int clearedCount) {
    }

    public static GameState reduce(GameState current, GameEvent event) {
        if (current.status() == GameStatus.GAME_OVER)
            return current;
        if (current.status() == GameStatus.ANIMATING_LINES && !(event instanceof GameEvent.LineAnimationEnd))
            return current;

        return switch (event) {
            case GameEvent.MoveLeft() -> handleMove(current, 0, -1);
            case GameEvent.MoveRight() -> handleMove(current, 0, 1);
            case GameEvent.MoveDown() -> handleMove(current, 1, 0);
            case GameEvent.Rotate() -> handleRotation(current);
            case GameEvent.TimeTick() -> handleGravity(current);
            case GameEvent.HardDrop() -> handleHardDrop(current);
            case GameEvent.LineAnimationEnd() -> handleLineAnimationEnd(current);
        };
    }

    private static GameState handleMove(GameState state, int dRow, int dCol) {
        Tetromino moved = PhysicsEngine.tryMove(state.board(), state.activePiece(), dRow, dCol);
        if (moved == state.activePiece())
            return state;
        return new GameState(state.board(), moved, state.nextQueue(),
                state.score(), state.linesCleared(), state.status(), state.seed(),
                Optional.empty());
    }

    private static GameState handleRotation(GameState state) {
        Tetromino rotated = PhysicsEngine.tryRotate(state.board(), state.activePiece());
        if (rotated == state.activePiece())
            return state;
        return new GameState(state.board(), rotated, state.nextQueue(),
                state.score(), state.linesCleared(), state.status(), state.seed(),
                Optional.empty());
    }

    private static GameState handleGravity(GameState state) {
        Tetromino currentPiece = state.activePiece();
        Tetromino movedDown = currentPiece.move(1, 0);
        if (state.board().isValidPosition(movedDown)) {
            return new GameState(state.board(), movedDown, state.nextQueue(),
                    state.score(), state.linesCleared(), state.status(), state.seed(),
                    Optional.empty());
        }

        Board mergedBoard = state.board().mergePiece(currentPiece);
        LineClearInfo clearInfo = detectFullLines(mergedBoard);
        boolean hasLinesToClear = clearInfo.clearedCount() > 0;

        if (!hasLinesToClear && isGameOverCondition(mergedBoard))
            return state.withStatus(GameStatus.GAME_OVER);

        // geração da nova peça e recarga do bag
        List<Tetromino.Shape> updatedQueue = new ArrayList<>(state.nextQueue());
        Tetromino.Shape nextShape = updatedQueue.isEmpty() ? Tetromino.Shape.I : updatedQueue.remove(0);
        long currentSeed = state.seed();
        if (updatedQueue.size() < 7) {
            var result = DeterministicGenerator.generateBag(currentSeed);
            updatedQueue.addAll(result.bag());
            currentSeed = result.nextSeed();
        }
        Tetromino newActivePiece = new Tetromino(nextShape, new MatrixPosition(0, 4), Tetromino.Orientation.NORTH);

        if (!mergedBoard.isValidPosition(newActivePiece))
            return new GameState(mergedBoard, newActivePiece, List.copyOf(updatedQueue),
                    state.score(), state.linesCleared(), GameStatus.GAME_OVER, currentSeed,
                    Optional.empty());

        GameStatus nextStatus = hasLinesToClear ? GameStatus.ANIMATING_LINES : GameStatus.PLAYING;
        if (hasLinesToClear) {
            return new GameState(mergedBoard, newActivePiece, List.copyOf(updatedQueue),
                    state.score(), state.linesCleared(), nextStatus, currentSeed,
                    Optional.of(clearInfo));
        } else {
            return new GameState(mergedBoard, newActivePiece, List.copyOf(updatedQueue),
                    state.score(), state.linesCleared(), nextStatus, currentSeed,
                    Optional.empty());
        }
    }

    private static GameState handleHardDrop(GameState state) {
        Tetromino current = state.activePiece();
        GameState step = state;
        while (step.board().isValidPosition(current.move(1, 0))) {
            current = current.move(1, 0);
            step = new GameState(step.board(), current, step.nextQueue(),
                    step.score() + 2, step.linesCleared(), step.status(), step.seed(),
                    Optional.empty());
        }
        return handleGravity(step);
    }

    private static GameState handleLineAnimationEnd(GameState state) {
        Optional<LineClearInfo> pendingOpt = state.pendingClear();
        if (pendingOpt.isEmpty())
            return state.withStatus(GameStatus.PLAYING);
        LineClearInfo clearInfo = pendingOpt.get();
        int clearedCount = clearInfo.clearedCount();
        Board newBoard = applyClear(state.board(), clearInfo);
        long scoreIncrement = computeScoreIncrement(clearedCount);
        return state.withClearedBoard(newBoard, scoreIncrement, clearedCount);
    }

    private static LineClearInfo detectFullLines(Board board) {
        Tetromino.Shape[][] matrix = board.matrix();
        int rows = Board.rows();
        int cols = Board.cols();
        List<Integer> fullIndices = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            boolean full = true;
            for (int c = 0; c < cols; c++) {
                if (matrix[r][c] == null) {
                    full = false;
                    break;
                }
            }
            if (full)
                fullIndices.add(r);
        }
        return new LineClearInfo(fullIndices.size(), fullIndices);
    }

    private static Board applyClear(Board board, LineClearInfo clearInfo) {
        Tetromino.Shape[][] original = board.matrix();
        int rows = Board.rows();
        int cols = Board.cols();
        boolean[] rowsToRemove = new boolean[rows];
        for (int idx : clearInfo.fullRowIndices()) {
            if (idx >= 0 && idx < rows)
                rowsToRemove[idx] = true;
        }
        Tetromino.Shape[][] newMatrix = new Tetromino.Shape[rows][cols];
        int targetRow = rows - 1;
        for (int r = rows - 1; r >= 0; r--) {
            if (!rowsToRemove[r]) {
                System.arraycopy(original[r], 0, newMatrix[targetRow], 0, cols);
                targetRow--;
            }
        }
        return Board.fromMatrix(newMatrix);
    }

    private static long computeScoreIncrement(int clearedCount) {
        return switch (clearedCount) {
            case 1 -> 100L;
            case 2 -> 300L;
            case 3 -> 500L;
            case 4 -> 800L;
            default -> 0L;
        };
    }

    private static boolean isGameOverCondition(Board board) {
        Tetromino.Shape[][] matrix = board.matrix();
        for (int c = 0; c < Board.cols(); c++) {
            if (matrix[0][c] != null)
                return true;
        }
        return false;
    }
}