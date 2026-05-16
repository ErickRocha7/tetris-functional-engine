package com.tetris.application;

import com.tetris.domain.GameEvent;
import com.tetris.domain.GameState;
import com.tetris.domain.GameStatus;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Agendador temporal e gravitacional da engine (Imperative Shell).
 * Trata a passagem do tempo estritamente como emissão linear de dados.
 * 
 * Agora também gerencia a saída do estado ANIMATING_LINES com detecção
 * de transição de estado, evitando múltiplos agendamentos e edge cases.
 */
public final class GameScheduler implements AutoCloseable {

    private final GameStore store;
    private final ScheduledExecutorService schedulerExecutor;
    private ScheduledFuture<?> currentTask;
    private long currentDelayMs;

    // Controle para evitar múltiplos agendamentos simultâneos
    private final AtomicBoolean lineAnimationScheduled = new AtomicBoolean(false);

    // Rastreia o último estado observado para detectar transições
    private volatile GameStatus lastStatus = null;

    public GameScheduler(GameStore store) {
        this.store = store;
        this.currentDelayMs = 500L; // Velocidade padrão inicial

        this.schedulerExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "game-clock-thread");
            thread.setDaemon(true);
            return thread;
        });

        // Observa o estado para reagir a entrada no estado ANIMATING_LINES
        this.store.subscribe(this::handleStateChange);
    }

    /**
     * Inicia ou reinicia o bombeamento constante de ticks temporais na fila da
     * Store.
     */
    public synchronized void start() {
        stop();

        this.currentTask = schedulerExecutor.scheduleAtFixedRate(
                () -> store.dispatch(new GameEvent.TimeTick()),
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
     * Ajusta dinamicamente a velocidade da gravidade.
     */
    public synchronized void updateSpeed(long newDelayMs) {
        if (this.currentDelayMs != newDelayMs && newDelayMs > 0) {
            this.currentDelayMs = newDelayMs;
            if (currentTask != null && !currentTask.isCancelled()) {
                start();
            }
        }
    }

    /**
     * Reage a mudanças de estado.
     * Detecta a transição para ANIMATING_LINES e agenda o fim da animação.
     */
    private void handleStateChange(GameState state) {
        GameStatus currentStatus = state.status();

        // Detecta se acabou de ENTRAR no estado ANIMATING_LINES
        boolean enteredAnimating = (currentStatus == GameStatus.ANIMATING_LINES
                && lastStatus != GameStatus.ANIMATING_LINES);

        if (enteredAnimating && lineAnimationScheduled.compareAndSet(false, true)) {
            schedulerExecutor.schedule(() -> {
                store.dispatch(new GameEvent.LineAnimationEnd());
                lineAnimationScheduled.set(false);
            }, 300, TimeUnit.MILLISECONDS);
        }

        // Atualiza o último estado observado
        lastStatus = currentStatus;
    }

    @Override
    public synchronized void close() {
        stop();
        this.schedulerExecutor.shutdownNow();
    }
}