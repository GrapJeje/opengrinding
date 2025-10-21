package nl.grapjeje.opengrinding.jobs.mining.configuration;

import lombok.Getter;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import nl.grapjeje.opengrinding.jobs.mining.objects.Ore;
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
public class MiningJobConfiguration extends JobConfig implements ShopConfig, LevelConfig {
    public record OreRecord(String name, Price price, int points, int unlockLevel) {}
    public record Pickaxe(String name, Price price, int unlockLevel) {}

    private boolean sellEnabled;
    private boolean openBuyShop;
    private boolean buyEnabled;

    private final Map<Ore, OreRecord> ores;
    private final Map<String, Pickaxe> pickaxes;

    private int maxLevel;
    private String formula;
    private final Map<Integer, Double> levelOverrides;

    public MiningJobConfiguration(File file) {
        super(file, "mining.yml", "default/jobs/mining.yml", true);

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
        this.openBuyShop = openBuy != null && openBuy.getBoolean("open-buy-shop", true);

        ConfigurationSection buySection = config.getConfigurationSection("economy.buy");
        this.buyEnabled = buySection != null && buySection.getBoolean("enabled", true);

        ores.clear();
        ConfigurationSection oreSection = config.getConfigurationSection("ores");
        if (oreSection != null) {
            for (String key : oreSection.getKeys(false)) {
                ConfigurationSection section = oreSection.getConfigurationSection(key);
                if (section != null) {
                    try {
                        Ore oreEnum = Ore.valueOf(key.toUpperCase());

                        ConfigurationSection priceSection = section.getConfigurationSection("sell-price");
                        double cash = priceSection != null ? priceSection.getDouble("cash", 0.0) : 0.0;
                        double tokens = priceSection != null ? priceSection.getDouble("tokens", 0.0) : 0.0;

                        OreRecord oreRecord = new OreRecord(
                                key,
                                new Price(cash, tokens),
                                section.getInt("points"),
                                section.getInt("unlock-level")
                        );
                        ores.put(oreEnum, oreRecord);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }

        pickaxes.clear();
        ConfigurationSection pickaxeSection = config.getConfigurationSection("pickaxes");
        if (pickaxeSection != null) {
            for (String key : pickaxeSection.getKeys(false)) {
                ConfigurationSection section = pickaxeSection.getConfigurationSection(key);
                if (section != null) {
                    ConfigurationSection priceSection = section.getConfigurationSection("price");
                    double cash = priceSection != null ? priceSection.getDouble("cash", 0.0) : 0.0;
                    double tokens = priceSection != null ? priceSection.getDouble("tokens", 0.0) : 0.0;

                    Pickaxe pickaxe = new Pickaxe(
                            key,
                            new Price(cash, tokens),
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
                    double value = overridesSection.getDouble(key);
                    levelOverrides.put(level, value);
                }
            }
        }
    }

    public OreRecord getOre(Ore ore) {
        return ores.get(ore);
    }

    public Pickaxe getPickaxe(String name) {
        return pickaxes.get(name.toLowerCase());
    }

    private final Map<Integer, Double> xpCache = new ConcurrentHashMap<>();
    private final ExecutorService xpExecutor = Executors.newSingleThreadExecutor();

    @Override
    public double getXpForLevel(int level) {
        if (levelOverrides.containsKey(level))
            return levelOverrides.get(level);
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