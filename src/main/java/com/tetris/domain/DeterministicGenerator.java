package com.tetris.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class DeterministicGenerator {

    /**
     * Registro que agrupa o resultado do sorteio e a próxima semente gerada.
     */
    public record GeneratorResult(List<Tetromino.Shape> bag, long nextSeed) {
    }

    /**
     * Função pura: dada uma semente, produz um Bag embaralhado e uma nova semente
     * determinística.
     */
    public static GeneratorResult generateBag(long currentSeed) {
        Random random = new Random(currentSeed);

        List<Tetromino.Shape> bag = new ArrayList<>(List.of(
                Tetromino.Shape.I, Tetromino.Shape.J, Tetromino.Shape.L,
                Tetromino.Shape.O, Tetromino.Shape.S, Tetromino.Shape.T, Tetromino.Shape.Z));

        Collections.shuffle(bag, random);

        // Gera a próxima semente de forma determinística para o próximo ciclo
        long nextSeed = random.nextLong();

        return new GeneratorResult(List.copyOf(bag), nextSeed);
    }
}
