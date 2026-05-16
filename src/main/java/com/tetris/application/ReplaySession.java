package com.tetris.application;

import com.tetris.domain.GameEvent;
import java.util.List;

/**
 * Estrutura imutável representando o arquivo completo de replay de uma partida.
 */
public record ReplaySession(
        long initialSeed,
        List<RecordedEvent> events) {
    public record RecordedEvent(long timestampMs, String eventType) {
    }
}
