package com.tetris.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameReducerTest {

    private GameState initialState;

    @BeforeEach
    void setUp() {
        // estado gerado deterministicamente pela seed 42L
        initialState = GameState.createInitial(42L);
    }

    @Test
    @DisplayName("Deve provar a pureza matemática: mesma entrada + mesmo evento = mesmo resultado")
    void testReducerReferentialTransparency() {
        GameEvent event = new GameEvent.MoveLeft();
        GameState state1 = GameReducer.reduce(initialState, event);
        GameState state2 = GameReducer.reduce(initialState, event);
        assertEquals(state1, state2);
    }

    @Test
    @DisplayName("Deve garantir a imutabilidade: o estado original nunca deve sofrer mutação após o reduce")
    void testReducerImmutability() {
        GameEvent event = new GameEvent.MoveRight();
        GameReducer.reduce(initialState, event);
        assertNull(initialState.board().matrix()[0][0]);
        assertEquals(new MatrixPosition(0, 4), initialState.activePiece().position());
    }

    @Test
    @DisplayName("Deve rejeitar comandos de movimentação de jogador se o estado for GAME_OVER")
    void testReducerGameOverLock() {
        GameState gameOverState = initialState.withStatus(GameStatus.GAME_OVER);
        GameEvent event = new GameEvent.MoveLeft();
        GameState nextState = GameReducer.reduce(gameOverState, event);
        assertEquals(gameOverState, nextState);
    }
}