package com.tetris.infrastructure.ui;

import com.tetris.domain.GameEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.util.Optional;

/**
 * Tradutor de infraestrutura (Input Shell).
 * Mapeia e converte estímulos de hardware do JavaFX em intenções abstratas do
 * domínio.
 * 
 * Agora com separação clara:
 * - MoveDown → input do jogador (soft drop)
 * - TimeTick → emitido exclusivamente pelo GameScheduler (gravidade)
 */
public final class InputMapper {

    /**
     * Analisa o KeyEvent físico e o transforma deterministicamente em um GameEvent
     * opcional.
     */
    public static Optional<GameEvent> map(KeyEvent keyEvent) {
        if (keyEvent == null) {
            return Optional.empty();
        }

        KeyCode code = keyEvent.getCode();
        GameEvent mappedEvent = switch (code) {
            case LEFT, A -> new GameEvent.MoveLeft();
            case RIGHT, D -> new GameEvent.MoveRight();
            case UP, W -> new GameEvent.Rotate();
            case DOWN, S -> new GameEvent.MoveDown(); // ✅ corrigido: soft drop
            case SPACE -> new GameEvent.HardDrop();
            default -> null;
        };

        return Optional.ofNullable(mappedEvent);
    }
}