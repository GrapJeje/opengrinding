package nl.grapjeje.opengrinding.jobs.mining.configuration;

import lombok.Getter;
import nl.grapjeje.core.Config;
import nl.grapjeje.opengrinding.jobs.core.configuration.JobConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class MiningJobConfiguration extends Config implements JobConfig {
    private boolean enabled;

    private boolean sellEnabled;
    private final Map<String, Double> sellPrices;

    private boolean buyEnabled;
    private final Map<String, Map<String, Double>> buyPrices;

    private int maxLevel;
    private int oreUnlockInterval;
    private final Map<String, Integer> pointsPerOre;
    private String formula;
    private final Map<Integer, Integer> levelOverrides;

    public MiningJobConfiguration(File file) {
        super(file, "mining.yml", "default/mining.yml", true);

        sellPrices = new LinkedHashMap<>();
        buyPrices = new LinkedHashMap<>();
        pointsPerOre = new LinkedHashMap<>();
        levelOverrides = new LinkedHashMap<>();
        this.values();
    }

    @Override
    public void values() {
        this.enabled = config.getBoolean("enabled", true);

        ConfigurationSection sellSection = config.getConfigurationSection("sell");
        if (sellSection != null) {
            this.sellEnabled = sellSection.getBoolean("enabled", true);
            for (String key : sellSection.getKeys(false)) {
                if (!key.equals("enabled"))
                    sellPrices.put(key, sellSection.getDouble(key));
            }
        } else this.sellEnabled = false;

        ConfigurationSection buySection = config.getConfigurationSection("buy");
        if (buySection != null) {
            this.buyEnabled = buySection.getBoolean("enabled", true);
            for (String itemKey : buySection.getKeys(false)) {
                if (!itemKey.equals("enabled")) {
                    ConfigurationSection itemSection = buySection.getConfigurationSection(itemKey);
                    if (itemSection != null) {
                        Map<String, Double> itemMap = new LinkedHashMap<>();
                        for (String tier : itemSection.getKeys(false)) {
                            itemMap.put(tier, itemSection.getDouble(tier));
                        }
                        buyPrices.put(itemKey, itemMap);
                    }
                }
            }
        } else this.buyEnabled = false;

        this.maxLevel = config.getInt("max-level", 40);
        this.oreUnlockInterval = config.getInt("ore-unlock-interval", 5);
        this.formula = config.getString("formula", "(100 * level) + ( (level / 5) * 50 )");

        for (String key : config.getKeys(false)) {
            if (key.startsWith("points-per-"))
                pointsPerOre.put(key.replace("points-per-", ""), config.getInt(key));
        }

        ConfigurationSection overridesSection = config.getConfigurationSection("level-overrides");
        if (overridesSection != null) {
            for (String key : overridesSection.getKeys(false)) {
                int level = Integer.parseInt(key);
                int value = overridesSection.getInt(key);
                levelOverrides.put(level, value);
            }
        }
    }

    public int getRequiredLevelForOre(String oreName) {
        return new ArrayList<>(pointsPerOre.keySet()).indexOf(oreName.toLowerCase()) * oreUnlockInterval;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public double getXpForLevel(int level) {
        if (levelOverrides.containsKey(level))
            return levelOverrides.get(level);
        return (100 * level) + (((double) level / 5) * 50);
    }

    @Override
    public boolean isSellEnabled() {
        return sellEnabled;
    }

    @Override
    public Integer getLevelOverride(int level) {
        return levelOverrides.getOrDefault(level, null);
    }
}
