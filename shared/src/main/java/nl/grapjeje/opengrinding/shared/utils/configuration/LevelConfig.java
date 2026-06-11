package nl.grapjeje.opengrinding.shared.utils.configuration;

public interface LevelConfig {
    int getMaxLevel();
    double getXpForLevel(int level);
    Double getLevelOverride(int level);
}
