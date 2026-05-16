package com.tetris.infrastructure.ui;

import com.tetris.application.EventInterceptor;
import com.tetris.application.GameScheduler;
import com.tetris.application.GameStore;
import com.tetris.application.ReplayPlayer;
import com.tetris.application.ReplaySession;
import com.tetris.application.MetricsAnalyzer;

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

import java.io.File;
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
        // Infraestrutura visual JavaFX
        // =========================================================

        Canvas canvas = new Canvas(300, 600);
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        TetrisIncrementalRenderer renderer = new TetrisIncrementalRenderer(canvas);

        boolean replayMode = false;

        try {
            File replayFile = new File("ultima_partida.replay.txt");

            // =====================================================
            // MODO CINEMA / REPLAY DETERMINÍSTICO
            // =====================================================
            if (replayFile.exists()) {

                ReplaySession session = EventInterceptor.load(
                        "ultima_partida.replay.txt");

                DeterministicGenerator.GeneratorResult firstBag = DeterministicGenerator
                        .generateBag(session.initialSeed());

                List<Tetromino.Shape> queue = new ArrayList<>(firstBag.bag());

                Tetromino.Shape firstShape = queue.remove(0);

                Tetromino firstPiece = new Tetromino(
                        firstShape,
                        new MatrixPosition(0, 4),
                        Tetromino.Orientation.NORTH);

                GameState replayInitialState = GameState.createInitial(
                        List.copyOf(queue),
                        firstPiece,
                        firstBag.nextSeed());

                this.store = new GameStore(replayInitialState);

                ReplayPlayer player = new ReplayPlayer(store);
                player.play("ultima_partida.replay.txt");

                replayMode = true;

                System.out.println("Engine rodando em modo REPLAY DETERMINÍSTICO.");
            }

        } catch (Exception e) {
            System.err.println("Falha ao inicializar replay: " + e.getMessage());
        }

        // =========================================================
        // Inicialização normal da engine
        // =========================================================

        if (!replayMode) {

            long initialSeed = 42L;

            DeterministicGenerator.GeneratorResult firstBag = DeterministicGenerator.generateBag(initialSeed);

            DeterministicGenerator.GeneratorResult secondBag = DeterministicGenerator.generateBag(firstBag.nextSeed());

            List<Tetromino.Shape> initialQueue = new ArrayList<>();
            initialQueue.addAll(firstBag.bag());
            initialQueue.addAll(secondBag.bag());

            Tetromino.Shape firstShape = initialQueue.remove(0);

            Tetromino firstPiece = new Tetromino(
                    firstShape,
                    new MatrixPosition(0, 4),
                    Tetromino.Orientation.NORTH);

            GameState initialState = GameState.createInitial(
                    List.copyOf(initialQueue),
                    firstPiece,
                    secondBag.nextSeed());

            this.store = new GameStore(initialState);
            this.scheduler = new GameScheduler(store);

            scene.setOnKeyPressed(keyEvent -> InputMapper.map(keyEvent).ifPresent(store::dispatch));

            this.scheduler.start();
        }

        // =========================================================
        // UI subscription
        // =========================================================

        store.subscribe(state -> Platform.runLater(() -> renderer.render(state)));

        primaryStage.setTitle("Functional Tetris Engine");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {

        if (scheduler != null) {
            scheduler.close();
        }

        if (store != null) {
            store.close();
        }

        // =========================================================
        // Relatório pós-execução (telemetria / replay analytics)
        // =========================================================
        Platform.runLater(() -> MetricsAnalyzer.generateReport("ultima_partida.replay.txt"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}