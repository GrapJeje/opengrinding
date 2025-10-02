package nl.grapjeje.opengrinding.jobs.fishing.configuration;

import lombok.Getter;
import nl.grapjeje.core.Config;
import nl.grapjeje.opengrinding.jobs.core.configuration.JobConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class FishingJobConfiguration extends Config implements JobConfig {
    private boolean enabled;

    private boolean sellEnabled;
    private boolean openBuyShop;
    private boolean buyEnabled;

    private int maxLevel;
    private String formula;
    private final Map<Integer, Integer> levelOverrides;

    public FishingJobConfiguration(File file) {
        super(file, "fishing.yml", "default/fishing.yml", true);

        levelOverrides = new LinkedHashMap<>();
        this.values();
    }

    @Override
    public void values() {
        this.enabled = config.getBoolean("enabled", true);

        ConfigurationSection sellSection = config.getConfigurationSection("economy.sell");
        this.sellEnabled = sellSection != null && sellSection.getBoolean("enabled", true);

        ConfigurationSection openBuy = config.getConfigurationSection("economy.sell");
        this.openBuyShop = openBuy != null && openBuy.getBoolean("open-buy-shop", true);

        ConfigurationSection buySection = config.getConfigurationSection("economy.buy");
        this.buyEnabled = buySection != null && buySection.getBoolean("enabled", true);

        ConfigurationSection levelSection = config.getConfigurationSection("level");
        if (levelSection != null) {
            this.maxLevel = levelSection.getInt("max", 40);
            this.formula = levelSection.getString("formula", "(100 * level) + ( (level / 5) * 50 )");

            ConfigurationSection overridesSection = levelSection.getConfigurationSection("overrides");
            if (overridesSection != null) {
                for (String key : overridesSection.getKeys(false)) {
                    int level = Integer.parseInt(key);
                    int value = overridesSection.getInt(key);
                    levelOverrides.put(level, value);
                }
            }
        }
    }

    @Override
    public double getXpForLevel(int level) {
        if (levelOverrides.containsKey(level))
            return levelOverrides.get(level);
        return (100 * level) + (((double) level / 5) * 50);
    }

    @Override
    public Integer getLevelOverride(int level) {
        return levelOverrides.getOrDefault(level, null);
    }
}
