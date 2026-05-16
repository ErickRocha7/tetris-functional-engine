package com.tetris.infrastructure.ui;

import com.tetris.application.GameScheduler;
import com.tetris.application.GameStore;
import com.tetris.domain.DeterministicGenerator;
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

        // =========================================================
        // Inicialização determinística do pipeline 7-Bag
        // =========================================================

        long initialSeed = 42L;

        DeterministicGenerator.GeneratorResult firstBag = DeterministicGenerator.generateBag(initialSeed);

        DeterministicGenerator.GeneratorResult secondBag = DeterministicGenerator.generateBag(firstBag.nextSeed());

        List<Tetromino.Shape> initialQueue = new ArrayList<>();

        initialQueue.addAll(firstBag.bag());
        initialQueue.addAll(secondBag.bag());

        // Primeira peça ativa da partida
        Tetromino.Shape firstShape = initialQueue.remove(0);

        Tetromino firstPiece = new Tetromino(
                firstShape,
                new MatrixPosition(0, 4),
                Tetromino.Orientation.NORTH);

        // Snapshot inicial imutável do domínio
        GameState initialState = GameState.createInitial(
                List.copyOf(initialQueue),
                firstPiece,
                secondBag.nextSeed());

        // =========================================================
        // Store central da aplicação
        // =========================================================

        this.store = new GameStore(initialState);

        // =========================================================
        // Scheduler temporal da engine
        // =========================================================

        this.scheduler = new GameScheduler(store);

        // =========================================================
        // Infraestrutura visual JavaFX
        // =========================================================

        Canvas canvas = new Canvas(300, 600);

        StackPane root = new StackPane(canvas);

        Scene scene = new Scene(root);

        // =========================================================
        // Renderer incremental reativo
        // =========================================================

        TetrisIncrementalRenderer renderer = new TetrisIncrementalRenderer(canvas);

        // =========================================================
        // Inscrição reativa da UI na Store
        // =========================================================

        store.subscribe(state -> {

            // Canal seguro de sincronização para a UI Thread
            Platform.runLater(() -> renderer.render(state));
        });

        // =========================================================
        // Captura desacoplada de input
        // =========================================================

        scene.setOnKeyPressed(keyEvent -> {

            InputMapper
                    .map(keyEvent)
                    .ifPresent(store::dispatch);
        });

        // =========================================================
        // Inicialização da janela
        // =========================================================

        primaryStage.setTitle("Functional Tetris Engine");

        primaryStage.setScene(scene);

        primaryStage.setResizable(false);

        primaryStage.show();

        // =========================================================
        // Disparo do loop temporal
        // =========================================================

        this.scheduler.start();
    }

    @Override
    public void stop() {

        // Encerramento seguro do ecossistema concorrente

        if (scheduler != null) {
            scheduler.close();
        }

        if (store != null) {
            store.close();
        }
    }

    public static void main(String[] args) {

        launch(args);
    }
}