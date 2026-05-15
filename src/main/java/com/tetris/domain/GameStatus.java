package com.tetris.domain;

/**
 * Máquina de estados formal da engine do jogo.
 * Controla declarativamente quais inputs e ações são válidos em cada fase.
 */
public enum GameStatus {
    /**
     * Jogo em andamento: aceita todos os comandos do jogador e ações de gravidade.
     */
    PLAYING,

    /**
     * Transição crítica de animação de linhas completas: bloqueia comandos do
     * jogador.
     */
    ANIMATING_LINES,

    /** Estado terminal da partida: nenhum comando é aceito. */
    GAME_OVER
}
