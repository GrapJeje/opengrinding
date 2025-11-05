package nl.grapjeje.opengrinding.jobs.farming.configuration;

import lombok.Getter;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import nl.grapjeje.opengrinding.jobs.farming.objects.Plant;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;
import nl.grapjeje.opengrinding.utils.configuration.LevelConfig;
import nl.grapjeje.opengrinding.utils.configuration.ShopConfig;
import nl.grapjeje.opengrinding.utils.currency.Price;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class FarmingJobConfiguration extends JobConfig implements LevelConfig, ShopConfig {
    public record PlantRecord(String name, Price prices, int points, int unlockLevel) {}
    public record Hoe(String name, Price price, int unlockLevel) {}

    private boolean sellEnabled;
    private boolean openBuyShop;
    private boolean buyEnabled;

    private final Map<Plant, PlantRecord> plants;
    private final Map<String, Hoe> hoes;

    private int maxLevel;
    private String formula;
    private final Map<Integer, Double> levelOverrides;

    public FarmingJobConfiguration(File file) {
        super(file, "farming.yml", "default/jobs/farming.yml", true);

        plants = new LinkedHashMap<>();
        hoes = new LinkedHashMap<>();
        levelOverrides = new LinkedHashMap<>();

        // Reset xpCache
        xpCache.clear();

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

        plants.clear();
        ConfigurationSection plantSection = config.getConfigurationSection("plants");
        if (plantSection != null) {
            for (String key : plantSection.getKeys(false)) {
                ConfigurationSection section = plantSection.getConfigurationSection(key);
                if (section != null) {
                    try {
                        Plant plantEnum = Plant.valueOf(key.toUpperCase());

                        ConfigurationSection priceSection = section.getConfigurationSection("sell-price");
                        double cash = priceSection != null ? priceSection.getDouble("cash", 0.0) : 0.0;
                        double tokens = priceSection != null ? priceSection.getDouble("tokens", 0.0) : 0.0;

                        PlantRecord plantRecord = new PlantRecord(
                                key,
                                new Price(cash, tokens),
                                section.getInt("points"),
                                section.getInt("unlock-level")
                        );
                        plants.put(plantEnum, plantRecord);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }

        hoes.clear();
        ConfigurationSection hoeSection = config.getConfigurationSection("hoes");
        if (hoeSection != null) {
            for (String key : hoeSection.getKeys(false)) {
                ConfigurationSection section = hoeSection.getConfigurationSection(key);
                if (section != null) {
                    ConfigurationSection priceSection = section.getConfigurationSection("price");
                    double cash = priceSection != null ? priceSection.getDouble("cash", 0.0) : 0.0;
                    double tokens = priceSection != null ? priceSection.getDouble("tokens", 0.0) : 0.0;

                    Hoe hoe = new Hoe(
                            key,
                            new Price(cash, tokens),
                            section.getInt("unlock-level")
                    );
                    hoes.put(key, hoe);
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
                    double value = overridesSection.getDouble(key);
                    levelOverrides.put(level, value);
                }
            }
        }
    }

    private final Map<Integer, Double> xpCache = new ConcurrentHashMap<>();
    private final ExecutorService xpExecutor = Executors.newSingleThreadExecutor();

    @Override
    public double getXpForLevel(int level) {
        if (xpCache.containsKey(level))
            return xpCache.get(level);
        try {
            Expression expression = new ExpressionBuilder(formula)
                    .variable("level")
                    .build()
                    .setVariable("level", level);

            double value = expression.evaluate();
            xpCache.put(level, value);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    @Override
    public Double getLevelOverride(int level) {
        return levelOverrides.getOrDefault(level, null);
    }
}
