package com.tetris.application;

import com.tetris.domain.GameEvent;
import com.tetris.domain.GameReducer;
import com.tetris.domain.GameState;

import java.util.List;
import java.util.CopyOnWriteArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * A Store central atua como a fonte única da verdade da aplicação.
 * Encapsula o estado, a fila sequencial de processamento e a infraestrutura
 * concorrente.
 */
public final class GameStore implements AutoCloseable {

    private final BlockingQueue<GameEvent> eventQueue;
    private final ExecutorService engineExecutor;
    private final AtomicReference<GameState> stateHolder;
    private final List<Consumer<GameState>> listeners;
    private volatile boolean running;

    public GameStore(GameState initialState) {
        this.eventQueue = new LinkedBlockingQueue<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.stateHolder = new AtomicReference<>(initialState);
        this.running = true;

        // Estabelece o Thread Confinement: apenas esta thread pode invocar o reducer
        this.engineExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "tetris-engine-thread");
            thread.setDaemon(true);
            return thread;
        });

        // Inicializa o laço de eventos assíncrono e linear
        this.engineExecutor.submit(this::runEventLoop);
    }

    /**
     * Canal central e thread-safe para publicação de intenções (Eventos) do jogo.
     */
    public void dispatch(GameEvent event) {
        if (running && event != null) {
            eventQueue.offer(event);
        }
    }

    /**
     * Permite que componentes assíncronos (como a UI) assinem o fluxo de novos
     * snapshots.
     */
    public void subscribe(Consumer<GameState> listener) {
        if (listener != null) {
            this.listeners.add(listener);
            // Envia o snapshot atual imediatamente no momento da inscrição
            listener.accept(stateHolder.get());
        }
    }

    /**
     * Permite a leitura não-bloqueante e atômica do snapshot de estado mais
     * recente.
     */
    public GameState getCurrentSnapshot() {
        return stateHolder.get();
    }

    /**
     * O Event Loop linearizado que consome a fila e executa as transições.
     */
    private void runEventLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Bloqueio eficiente: aguarda um evento sem consumir CPU (evita busy waiting)
                GameEvent event = eventQueue.take();

                GameState currentState = stateHolder.get();
                GameState nextState = GameReducer.reduce(currentState, event);

                // Publicação atômica se houve transição real de dados
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
     * Propaga o novo snapshot isolando falhas lançadas por listeners periféricos.
     */
    private void notifyListeners(GameState state) {
        for (Consumer<GameState> listener : listeners) {
            try {
                listener.accept(state);
            } catch (Exception e) {
                // Impede que erros visuais de renderização derrubem o loop da engine
                System.err.println("Erro isolado no StateListener: " + e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        this.running = false;
        this.engineExecutor.shutdownNow();
    }
}
