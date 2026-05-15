package com.tetris.application;

import com.tetris.domain.GameEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Agendador temporal e gravitacional da engine (Imperative Shell).
 * Trata a passagem do tempo estritamente como emissão linear de dados.
 */
public final class GameScheduler implements AutoCloseable {

    private final GameStore store;
    private final ScheduledExecutorService schedulerExecutor;
    private ScheduledFuture<?> currentTask;
    private long currentDelayMs;

    public GameScheduler(GameStore store) {
        this.store = store;
        this.currentDelayMs = 500L; // Velocidade padrão inicial (meio segundo por tick)

        // Confina o relógio físico em uma thread daemon dedicada isolada da engine e da
        // UI
        this.schedulerExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "game-clock-thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Inicia ou reinicia o bombeamento constante de ticks temporais na fila da
     * Store.
     */
    public synchronized void start() {
        stop();

        this.currentTask = schedulerExecutor.scheduleAtFixedRate(
                () -> store.dispatch(new GameEvent.TimeTick()), // Transforma o tempo em dado puro
                currentDelayMs,
                currentDelayMs,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Interrompe temporariamente a emissão de ticks (Pausa).
     */
    public synchronized void stop() {
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel(false);
        }
    }

    /**
     * Ajusta dinamicamente a velocidade da gravidade (ex: aceleração ao subir de
     * nível)
     * sem causar descontinuidades ou sobreposição de agendamentos.
     */
    public synchronized void updateSpeed(long newDelayMs) {
        if (this.currentDelayMs != newDelayMs && newDelayMs > 0) {
            this.currentDelayMs = newDelayMs;
            if (currentTask != null && !currentTask.isCancelled()) {
                start(); // Reinicia o agendamento aplicando o novo ritmo matemático
            }
        }
    }

    @Override
    public synchronized void close() {
        stop();
        this.schedulerExecutor.shutdownNow();
    }
}
