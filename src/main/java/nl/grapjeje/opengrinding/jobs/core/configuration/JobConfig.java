package nl.grapjeje.opengrinding.jobs.core.configuration;

public interface JobConfig {
    int getMaxLevel();
    double getXpForLevel(int level);
    boolean isSellEnabled();
    Integer getLevelOverride(int level);
}
