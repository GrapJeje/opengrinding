package nl.grapjeje.opengrinding.jobs.farming.configuration;

import lombok.Getter;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;
import nl.grapjeje.opengrinding.utils.configuration.LevelConfig;
import nl.grapjeje.opengrinding.utils.configuration.ShopConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class FarmingJobConfiguration extends JobConfig implements LevelConfig, ShopConfig {
    private boolean sellEnabled;
    private boolean openBuyShop;
    private boolean buyEnabled;

    private int maxLevel;
    private String formula;
    private final Map<Integer, Double> levelOverrides;

    public FarmingJobConfiguration(File file) {
        super(file, "farming.yml", "default/jobs/farming.yml", true);

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
