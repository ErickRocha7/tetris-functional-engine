package com.tetris.application;

import com.tetris.domain.GameEvent;
import com.tetris.domain.GameState;
import com.tetris.domain.MatrixPosition;
import com.tetris.domain.Tetromino;
import com.tetris.domain.DeterministicGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Player de Replay Automatizado (Modo Cinema).
 * Reproduz o pipeline original reinjetando dados discretos com precisão temporal.
 */
public final class ReplayPlayer implements AutoCloseable {

    private final GameStore store;
    private final ScheduledExecutorService replayExecutor;

    public ReplayPlayer(GameStore store) {
        this.store = store;
        this.replayExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "replay-cinema-thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Carrega as instruções gravadas e reconstrói a partida com precisão de milissegundos.
     */
    public void play(String filename) throws IOException {
        ReplaySession session = EventInterceptor.load(filename);

        System.out.println("Inicializando Modo Cinema. Semente Original: " + session.initialSeed());

        // Agenda dinamicamente a injeção de cada evento de acordo com o timestamp original
        for (ReplaySession.RecordedEvent recorded : session.events()) {
            GameEvent domainEvent = instantiateEvent(recorded.eventType());
            
            if (domainEvent != null) {
                replayExecutor.schedule(
                    () -> store.dispatch(domainEvent),
                    recorded.timestampMs(),
                    TimeUnit.MILLISECONDS
                );
            }
        }
    }

    /**
     * Reconstrói o Tipo Algébrico correspondente a partir do texto lido do log.
     */
    private GameEvent instantiateEvent(String type) {
        return switch (type) {
            case "MoveLeft" -> new GameEvent.MoveLeft();
            case "MoveRight" -> new GameEvent.MoveRight();
            case "Rotate" -> new GameEvent.Rotate();
            case "HardDrop" -> new GameEvent.HardDrop();
            case "TimeTick" -> new GameEvent.TimeTick();
            case "LineAnimationEnd" -> new GameEvent.LineAnimationEnd();
            default -> null;
        };
    }

    @Override
    public void close() {
        this.replayExecutor.shutdownNow();
    }
}
