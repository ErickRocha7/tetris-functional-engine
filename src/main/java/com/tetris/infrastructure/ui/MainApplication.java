package com.tetris.infrastructure.ui;

import com.tetris.application.EventInterceptor;
import com.tetris.application.GameScheduler;
import com.tetris.application.GameStore;
import com.tetris.application.ReplayPlayer;
import com.tetris.application.ReplaySession;
import com.tetris.application.MetricsAnalyzer;
import com.tetris.domain.GameState;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.File;

/**
 * Ponto de entrada e orquestrador de inicialização da infraestrutura da Engine.
 * Agora totalmente determinístico: o estado inicial é gerado apenas pela seed.
 */
public final class MainApplication extends Application {

    private GameStore store;
    private GameScheduler scheduler;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(300, 600);
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);
        TetrisIncrementalRenderer renderer = new TetrisIncrementalRenderer(canvas);

        boolean replayMode = false;
        long initialSeed = 42L; // seed padrão para novo jogo

        try {
            File replayFile = new File("ultima_partida.replay.txt");
            if (replayFile.exists()) {
                ReplaySession session = EventInterceptor.load("ultima_partida.replay.txt");
                initialSeed = session.initialSeed();
                replayMode = true;
                System.out.println("Modo REPLAY – seed: " + initialSeed);
            }
        } catch (Exception e) {
            System.err.println("Falha ao carregar replay: " + e.getMessage());
        }

        // Cria o estado inicial unicamente a partir da seed (o domínio gera todo o
        // resto)
        GameState initialState = GameState.createInitial(initialSeed);
        this.store = new GameStore(initialState);

        if (!replayMode) {
            // Modo normal: ativa inputs do jogador e o scheduler de tempo
            this.scheduler = new GameScheduler(store);
            scene.setOnKeyPressed(keyEvent -> InputMapper.map(keyEvent).ifPresent(store::dispatch));
            this.scheduler.start();
        } else {
            // Modo replay: apenas injeta os eventos gravados (não precisa de scheduler)
            try {
                ReplayPlayer player = new ReplayPlayer(store);
                player.play("ultima_partida.replay.txt");
            } catch (Exception e) {
                System.err.println("Erro ao executar replay: " + e.getMessage());
            }
        }

        store.subscribe(state -> Platform.runLater(() -> renderer.render(state)));

        primaryStage.setTitle("Functional Tetris Engine");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (scheduler != null)
            scheduler.close();
        if (store != null)
            store.close();
        Platform.runLater(() -> MetricsAnalyzer.generateReport("ultima_partida.replay.txt"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}