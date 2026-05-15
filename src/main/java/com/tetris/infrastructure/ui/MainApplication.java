package com.tetris.infrastructure.ui;

import com.tetris.application.GameScheduler;
import com.tetris.application.GameStore;
import com.tetris.domain.GameState;
import com.tetris.domain.MatrixPosition;
import com.tetris.domain.Tetromino;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Ponto de entrada e orquestrador de inicialização da infraestrutura da Engine.
 */
public final class MainApplication extends Application {

    private GameStore store;
    private GameScheduler scheduler;

    @Override
    public void start(Stage primaryStage) {
        // 1. Instancia o estado de domínio inicial para a partida
        List<Tetromino.Shape> initialQueue = new ArrayList<>(List.of(
                Tetromino.Shape.T, Tetromino.Shape.O, Tetromino.Shape.I, Tetromino.Shape.J));
        Tetromino firstPiece = new Tetromino(
                Tetromino.Shape.L,
                new MatrixPosition(0, 4),
                Tetromino.Orientation.NORTH);
        GameState initialState = GameState.createInitial(initialQueue, firstPiece);

        // 2. Inicializa a Store Central (Thread Confinement / Event Loop)
        this.store = new GameStore(initialState);

        // 3. Inicializa o Scheduler Temporal (Tempo como Dado)
        this.scheduler = new GameScheduler(store);

        // 4. Monta a árvore de componentes visuais do JavaFX (Canvas)
        Canvas canvas = new Canvas(300, 600); // 10 colunas x 20 linhas (30px cada célula)
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        // 5. Inicializa o Renderizador Incremental Reativo
        TetrisIncrementalRenderer renderer = new TetrisIncrementalRenderer(canvas);

        // 6. Inscreve a UI na Store para receber e projetar snapshots imutáveis
        store.subscribe(state -> {
            // Canaliza a atualização vinda da background thread com segurança para a UI
            // Thread
            Platform.runLater(() -> renderer.render(state));
        });

        // 7. Configura a captação de Hardware Input desacoplada via InputMapper
        scene.setOnKeyPressed(keyEvent -> {
            InputMapper.map(keyEvent).ifPresent(store::dispatch);
        });

        // 8. Inicializa a janela física e dispara o relógio da engine
        primaryStage.setTitle("Functional Tetris Engine");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        this.scheduler.start();
    }

    @Override
    public void stop() {
        // Encerramento limpo e seguro de todas as threads concorrentes da aplicação
        if (scheduler != null)
            scheduler.close();
        if (store != null)
            store.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
