package com.tetris.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameReducerTest {

    private GameState initialState;

    @BeforeEach
    void setUp() {
        List<Tetromino.Shape> queue = new ArrayList<>(List.of(Tetromino.Shape.I, Tetromino.Shape.O, Tetromino.Shape.T));
        Tetromino active = new Tetromino(Tetromino.Shape.T, new MatrixPosition(0, 4), Tetromino.Orientation.NORTH);
        initialState = GameState.createInitial(queue, active, 42L);
    }

    @Test
    @DisplayName("Deve provar a pureza matemática: mesma entrada + mesmo evento = mesmo resultado")
    void testReducerReferentialTransparency() {
        GameEvent event = new GameEvent.MoveLeft();

        // Duas reduções isoladas a partir do exato mesmo snapshot estável
        GameState state1 = GameReducer.reduce(initialState, event);
        GameState state2 = GameReducer.reduce(initialState, event);

        // Provando determinismo referencial
        assertEquals(state1, state2);
    }

    @Test
    @DisplayName("Deve garantir a imutabilidade: o estado original nunca deve sofrer mutação após o reduce")
    void testReducerImmutability() {
        GameEvent event = new GameEvent.MoveRight();

        GameReducer.reduce(initialState, event);

        // O estado original deve permanecer rigorosamente idêntico à sua criação
        assertEquals(0, initialState.board().matrix()[0][0] == null ? 0 : 1);
        assertEquals(new MatrixPosition(0, 4), initialState.activePiece().position());
    }

    @Test
    @DisplayName("Deve rejeitar comandos de movimentação de jogador se o estado for GAME_OVER")
    void testReducerGameOverLock() {
        GameState gameOverState = initialState.withStatus(GameStatus.GAME_OVER);
        GameEvent event = new GameEvent.MoveLeft();

        GameState nextState = GameReducer.reduce(gameOverState, event);

        // Nenhuma peça deve se deslocar ou alterar dados no estado terminal
        assertEquals(gameOverState, nextState);
    }
}
