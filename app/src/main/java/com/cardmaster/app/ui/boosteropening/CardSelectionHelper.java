package com.cardmaster.app.ui.boosteropening;

import java.util.Random;

public class CardSelectionHelper {

    // Probability matrix: cumulative values for (upgrade, rarity) pairs
    private static final double[][] PROBABILITY_MATRIX = {
        // {upgrade, rarity, cumulative_probability}
        {0, 1, 0.350000},
        {0, 2, 0.525000},
        {1, 1, 0.615000},
        {0, 3, 0.702500},
        {2, 1, 0.745000},
        {0, 4, 0.788750},
        {1, 2, 0.833750},
        {0, 5, 0.861750},
        {1, 3, 0.884250},
        {0, 6, 0.900000},
        {3, 1, 0.917500},
        {2, 2, 0.938750},
        {1, 4, 0.950000},
        {2, 3, 0.960625},
        {3, 2, 0.969375},
        {1, 5, 0.976575},
        {1, 6, 0.980625},
        {2, 4, 0.985938},
        {3, 3, 0.990313},
        {2, 5, 0.993713},
        {3, 4, 0.995901},
        {2, 6, 0.997814},
        {3, 5, 0.999214},
        {3, 6, 1.000002}
    };

    private final Random random;

    public CardSelectionHelper() {
        this.random = new Random();
    }

    public CardSelectionResult selectCard() {
        double randomValue = random.nextDouble();
        
        for (double[] entry : PROBABILITY_MATRIX) {
            if (randomValue <= entry[2]) {
                return new CardSelectionResult((int) entry[0], (int) entry[1]);
            }
        }
        
        // Fallback to lowest probability if something goes wrong
        return new CardSelectionResult(3, 6);
    }

    public static class CardSelectionResult {
        public final int upgrade;
        public final int rarity;

        public CardSelectionResult(int upgrade, int rarity) {
            this.upgrade = upgrade;
            this.rarity = rarity;
        }
    }
}
