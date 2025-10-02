package nl.grapjeje.opengrinding.jobs.core.configuration;

public interface JobConfig {
    int getMaxLevel();
    double getXpForLevel(int level);
    boolean isSellEnabled();
    boolean isBuyEnabled();
    Integer getLevelOverride(int level);
}
