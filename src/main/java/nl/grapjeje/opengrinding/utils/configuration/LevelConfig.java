package nl.grapjeje.opengrinding.utils.configuration;

public interface LevelConfig {
    int getMaxLevel();
    double getXpForLevel(int level);
    Double getLevelOverride(int level);
}
