package nl.grapjeje.opengrinding.utils.configuration;

public interface LevelConfig {
    int getMaxLevel();
    double getXpForLevel(int level);
    Integer getLevelOverride(int level);
}
