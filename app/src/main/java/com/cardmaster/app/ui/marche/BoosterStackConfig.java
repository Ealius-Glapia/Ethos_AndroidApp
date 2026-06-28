package com.cardmaster.app.ui.marche;

public class BoosterStackConfig {
    public int count;
    public int price;

    public BoosterStackConfig(int count, int price) {
        this.count = count;
        this.price = price;
    }

    // Default configuration - easily modifiable
    public static BoosterStackConfig[] getDefaultStacks() {
        return new BoosterStackConfig[]{
            new BoosterStackConfig(1, 6480),
            new BoosterStackConfig(5, 20480),
            new BoosterStackConfig(10, 32805),
            new BoosterStackConfig(20, 50000)
        };
    }
}
