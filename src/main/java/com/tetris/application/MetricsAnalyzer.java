package com.tetris.application;

import java.io.IOException;
import java.util.List;

/**
 * Motor de Auditoria e Telemetria (Application Shell).
 * Analisa logs históricos de eventos para extrair estatísticas avançadas de
 * performance (APM).
 */
public final class MetricsAnalyzer {

    /**
     * Processa um arquivo de log gravado e imprime um relatório de telemetria no
     * console.
     */
    public static void generateReport(String filename) {
        try {
            ReplaySession session = EventInterceptor.load(filename);
            List<ReplaySession.RecordedEvent> events = session.events();

            if (events.isEmpty()) {
                System.out.println("Relatório de Telemetria: Nenhum evento registrado na partida.");
                return;
            }

            long totalDurationMs = events.get(events.size() - 1).timestampMs();
            double totalDurationMinutes = totalDurationMs / 60000.0;

            long humanInputsCount = 0;
            long gravityTicksCount = 0;
            long lineClearsCount = 0;

            // Filtra os tipos algébricos para computação estatística isolada
            for (ReplaySession.RecordedEvent event : events) {
                switch (event.eventType()) {
                    case "MoveLeft", "MoveRight", "Rotate", "HardDrop" -> humanInputsCount++;
                    case "TimeTick" -> gravityTicksCount++;
                    case "LineAnimationEnd" -> lineClearsCount++;
                    default -> {
                    }
                }
            }

            // Calcula o APM baseado no tempo total decorrido da simulação
            double apm = totalDurationMinutes > 0 ? (humanInputsCount / totalDurationMinutes) : 0.0;

            // Renderiza o relatório estruturado de forma linear e limpa
            System.out.println("=================================================");
            System.out.println("     RELATÓRIO DE AUDITORIA DE PERFORMANCE       ");
            System.out.println("=================================================");
            System.out.println(String.format("Duração Total da Partida : %.2f segundos", totalDurationMs / 1000.0));
            System.out.println("Semente Matemática (Seed): " + session.initialSeed());
            System.out.println("Total de Gravidade (Ticks): " + gravityTicksCount);
            System.out.println("Linhas Eliminadas (Clears): " + lineClearsCount);
            System.out.println("-------------------------------------------------");
            System.out.println("Total de Comandos Humanos : " + humanInputsCount);
            System.out.println(String.format("Ações por Minuto (APM)   : %.2f", apm));
            System.out.println("=================================================");

        } catch (IOException e) {
            System.err.println("Falha ao ler dados de telemetria para o relatório: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao processar relatório de APM: " + e.getMessage());
        }
    }
}
