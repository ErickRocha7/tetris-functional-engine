package com.tetris.application;

import com.tetris.domain.GameEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Interceptador estruturado responsável por auditar e persistir o fluxo de
 * eventos.
 */
public final class EventInterceptor {

    private final long initialSeed;
    private final long startTime;
    private final List<String> logBuffer;

    public EventInterceptor(long initialSeed) {
        this.initialSeed = initialSeed;
        this.startTime = System.currentTimeMillis();
        this.logBuffer = new ArrayList<>();
        // Registra o cabeçalho determinístico contendo a semente
        this.logBuffer.add("SEED:" + initialSeed);
    }

    /**
     * Intercepta e armazena em memória um evento processado pelo loop da Store.
     */
    public synchronized void intercept(GameEvent event) {
        if (event == null)
            return;

        long elapsed = System.currentTimeMillis() - startTime;
        String eventName = event.getClass().getSimpleName();

        // Formato de linha: delta_tempo:NOME_DO_EVENTO
        this.logBuffer.add(elapsed + ":" + eventName);
    }

    /**
     * Grava definitivamente os dados acumulados no arquivo de log do replay.
     */
    public synchronized void persist(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String line : logBuffer) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Replay gravado com sucesso em: " + filename);
        } catch (IOException e) {
            System.err.println("Falha ao gravar arquivo de replay: " + e.getMessage());
        }
    }

    /**
     * Lê um arquivo de replay e o reconstrói estruturadamente em memória.
     */
    public static ReplaySession load(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filename));
        if (lines.isEmpty() || !lines.get(0).startsWith("SEED:")) {
            throw new IllegalArgumentException("Formato de arquivo de replay inválido.");
        }

        long seed = Long.parseLong(lines.get(0).split(":")[1]);
        List<ReplaySession.RecordedEvent> recordedEvents = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(":");
            if (parts.length == 2) {
                long ts = Long.parseLong(parts[0]);
                recordedEvents.add(new ReplaySession.RecordedEvent(ts, parts[1]));
            }
        }

        return new ReplaySession(seed, List.copyOf(recordedEvents));
    }
}
