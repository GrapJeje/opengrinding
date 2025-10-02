package nl.grapjeje.opengrinding.jobs.mining.configuration;

import lombok.Getter;
import nl.grapjeje.core.Config;
import nl.grapjeje.opengrinding.jobs.core.configuration.JobConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class MiningJobConfiguration extends Config implements JobConfig {
    public record Ore(String name, double sellPrice, int points, int unlockLevel) {}
    public record Pickaxe(String name, double price, int unlockLevel) {}

    private boolean enabled;

    private boolean sellEnabled;
    private boolean openBuyShop;
    private boolean buyEnabled;

    private final Map<String, Ore> ores;
    private final Map<String, Pickaxe> pickaxes;

    private int maxLevel;
    private String formula;
    private final Map<Integer, Integer> levelOverrides;

    public MiningJobConfiguration(File file) {
        super(file, "mining.yml", "default/mining.yml", true);

        ores = new LinkedHashMap<>();
        pickaxes = new LinkedHashMap<>();
        levelOverrides = new LinkedHashMap<>();

        this.values();
    }

    @Override
    public void values() {
        this.enabled = config.getBoolean("enabled", true);

        ConfigurationSection sellSection = config.getConfigurationSection("economy.sell");
        this.sellEnabled = sellSection != null && sellSection.getBoolean("enabled", true);

        ConfigurationSection openBuy = config.getConfigurationSection("economy.sell");
        this.buyEnabled = openBuy != null && openBuy.getBoolean("open-buy-shop", true);

        ConfigurationSection buySection = config.getConfigurationSection("economy.buy");
        this.buyEnabled = buySection != null && buySection.getBoolean("enabled", true);

        ores.clear();
        ConfigurationSection oreSection = config.getConfigurationSection("ores");
        if (oreSection != null) {
            for (String key : oreSection.getKeys(false)) {
                ConfigurationSection section = oreSection.getConfigurationSection(key);
                if (section != null) {
                    Ore ore = new Ore(
                            key,
                            section.getDouble("sell-price"),
                            section.getInt("points"),
                            section.getInt("unlock-level")
                    );
                    ores.put(key, ore);
                }
            }
        }

        pickaxes.clear();
        ConfigurationSection pickaxeSection = config.getConfigurationSection("pickaxes");
        if (pickaxeSection != null) {
            for (String key : pickaxeSection.getKeys(false)) {
                ConfigurationSection section = pickaxeSection.getConfigurationSection(key);
                if (section != null) {
                    Pickaxe pickaxe = new Pickaxe(
                            key,
                            section.getDouble("price"),
                            section.getInt("unlock-level")
                    );
                    pickaxes.put(key, pickaxe);
                }
            }
        }

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

    public Ore getOre(String name) {
        return ores.get(name.toLowerCase());
    }

    public Pickaxe getPickaxe(String name) {
        return pickaxes.get(name.toLowerCase());
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

