package nl.grapjeje.opengrinding.jobs.lumber.configuration;

import lombok.Getter;
import nl.grapjeje.opengrinding.jobs.lumber.objects.Wood;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;
import nl.grapjeje.opengrinding.utils.configuration.LevelConfig;
import nl.grapjeje.opengrinding.utils.configuration.ShopConfig;
import nl.grapjeje.opengrinding.utils.currency.Price;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class LumberJobConfiguration extends JobConfig implements ShopConfig, LevelConfig {
    public record WoodRecord(String name, Map<String, Price> prices, int points, Map<String, Integer> unlockLevels) {}
    public record Axe(String name, Price price, int unlockLevel) {}

    private boolean sellEnabled;
    private boolean openBuyShop;
    private boolean buyEnabled;

    private final Map<Wood, WoodRecord> woods;
    private final Map<String, Axe> axes;

    private int maxLevel;
    private String formula;
    private final Map<Integer, Integer> levelOverrides;

    public LumberJobConfiguration(File file) {
        super(file, "lumber.yml", "default/jobs/lumber.yml", true);

        woods = new LinkedHashMap<>();
        axes = new LinkedHashMap<>();
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

        woods.clear();
        ConfigurationSection woodsSection = config.getConfigurationSection("woods");
        if (woodsSection != null) {
            for (String key : woodsSection.getKeys(false)) {
                ConfigurationSection section = woodsSection.getConfigurationSection(key);
                if (section != null) {
                    try {
                        Wood woodEnum = Wood.valueOf(key.toUpperCase());
                        Map<String, Price> prices = new LinkedHashMap<>();
                        ConfigurationSection priceSection = section.getConfigurationSection("sell-price");
                        if (priceSection != null) {
                            for (String type : priceSection.getKeys(false)) {
                                ConfigurationSection typeSection = priceSection.getConfigurationSection(type);
                                if (typeSection != null) {
                                    double cash = typeSection.getDouble("cash", 0.0);
                                    double tokens = typeSection.getDouble("tokens", 0.0);
                                    prices.put(type, new Price(cash, tokens));
                                }
                            }
                        }
                        Map<String, Integer> unlockLevels = new LinkedHashMap<>();
                        for (String type : section.getKeys(false)) {
                            if (!type.equals("sell-price") && !type.equals("points"))
                                unlockLevels.put(type, section.getConfigurationSection(type).getInt("unlock-level", 0));
                        }
                        WoodRecord woodRecord = new WoodRecord(
                                key,
                                prices,
                                section.getInt("points"),
                                unlockLevels
                        );
                        woods.put(woodEnum, woodRecord);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }

        axes.clear();
        ConfigurationSection axeSection = config.getConfigurationSection("axes");
        if (axeSection != null) {
            for (String key : axeSection.getKeys(false)) {
                ConfigurationSection section = axeSection.getConfigurationSection(key);
                if (section != null) {
                    ConfigurationSection priceSection = section.getConfigurationSection("price");
                    double cash = priceSection != null ? priceSection.getDouble("cash", 0.0) : 0.0;
                    double tokens = priceSection != null ? priceSection.getDouble("tokens", 0.0) : 0.0;

                    Axe axe = new Axe(
                            key,
                            new Price(cash, tokens),
                            section.getInt("unlock-level")
                    );
                    axes.put(key, axe);
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

    public WoodRecord getWood(Wood wood) {
        return woods.get(wood);
    }

    public Axe getAxe(String name) {
        return axes.get(name.toLowerCase());
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
