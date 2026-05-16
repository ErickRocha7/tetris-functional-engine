package com.tetris.application;

import com.tetris.domain.GameEvent;
import com.tetris.domain.GameReducer;
import com.tetris.domain.GameState;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * A Store central atua como a fonte única da verdade da aplicação.
 * Encapsula o estado, a fila sequencial de processamento
 * e a infraestrutura concorrente.
 */
public final class GameStore implements AutoCloseable {

    private final BlockingQueue<GameEvent> eventQueue;

    private final ExecutorService engineExecutor;

    private final AtomicReference<GameState> stateHolder;

    private final List<Consumer<GameState>> listeners;

    /**
     * Interceptador determinístico de eventos
     * responsável por registrar a trilha temporal da partida.
     */
    private final EventInterceptor interceptor;

    private volatile boolean running;

    public GameStore(GameState initialState) {

        this.eventQueue = new LinkedBlockingQueue<>();

        this.listeners = new CopyOnWriteArrayList<>();

        this.stateHolder = new AtomicReference<>(initialState);

        // Inicializa o interceptador com a seed da partida
        this.interceptor = new EventInterceptor(initialState.seed());

        this.running = true;

        /**
         * Thread confinement:
         * apenas esta thread executa o reducer.
         */
        this.engineExecutor = Executors.newSingleThreadExecutor(runnable -> {

            Thread thread = new Thread(runnable, "tetris-engine-thread");

            thread.setDaemon(true);

            return thread;
        });

        // Inicializa o Event Loop linearizado
        this.engineExecutor.submit(this::runEventLoop);
    }

    /**
     * Canal central thread-safe de publicação de eventos.
     */
    public void dispatch(GameEvent event) {

        if (running && event != null) {

            eventQueue.offer(event);
        }
    }

    /**
     * Permite inscrição reativa no fluxo de snapshots.
     */
    public void subscribe(Consumer<GameState> listener) {

        if (listener != null) {

            this.listeners.add(listener);

            // Publica imediatamente o snapshot atual
            listener.accept(stateHolder.get());
        }
    }

    /**
     * Leitura atômica do snapshot atual.
     */
    public GameState getCurrentSnapshot() {

        return stateHolder.get();
    }

    /**
     * Event Loop centralizado da engine.
     */
    private void runEventLoop() {

        while (running && !Thread.currentThread().isInterrupted()) {

            try {

                /**
                 * Bloqueio eficiente:
                 * aguarda eventos sem busy waiting.
                 */
                GameEvent event = eventQueue.take();

                // =====================================================
                // Interceptação determinística do evento
                // =====================================================

                this.interceptor.intercept(event);

                GameState currentState = stateHolder.get();

                // =====================================================
                // Redução funcional do estado
                // =====================================================

                GameState nextState = GameReducer.reduce(
                        currentState,
                        event);

                // =====================================================
                // Publicação atômica do novo snapshot
                // =====================================================

                if (nextState != currentState) {

                    stateHolder.set(nextState);

                    notifyListeners(nextState);
                }

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                break;
            }
        }
    }

    /**
     * Propaga snapshots para listeners externos
     * isolando falhas periféricas.
     */
    private void notifyListeners(GameState state) {

        for (Consumer<GameState> listener : listeners) {

            try {

                listener.accept(state);

            } catch (Exception e) {

                /**
                 * Isolamento de falha:
                 * erros periféricos não derrubam a engine.
                 */
                System.err.println(
                        "Erro isolado no StateListener: "
                                + e.getMessage());
            }
        }
    }

    @Override
    public void close() {

        this.running = false;

        this.engineExecutor.shutdownNow();

        // Salva automaticamente a trilha temporal da partida
        this.interceptor.persist(
                "ultima_partida.replay.txt");
    }
}