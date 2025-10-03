package nl.grapjeje.opengrinding.utils;

public interface JobConfig {
    int getMaxLevel();
    double getXpForLevel(int level);
    boolean isSellEnabled();
    boolean isBuyEnabled();
    Integer getLevelOverride(int level);
}
