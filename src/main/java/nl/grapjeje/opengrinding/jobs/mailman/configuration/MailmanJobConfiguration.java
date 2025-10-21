package nl.grapjeje.opengrinding.jobs.mailman.configuration;

import lombok.Getter;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;
import nl.grapjeje.opengrinding.utils.configuration.LevelConfig;
import nl.grapjeje.opengrinding.utils.currency.Price;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class MailmanJobConfiguration extends JobConfig implements LevelConfig {
    private String name;

    private final List<Material> blockWhitelist;
    public record Amount(int min, int max) {}
    public record Package(int level, int waitTime, Price price, Amount amount) {}

    private final Map<Integer, Package> packages;

    private int maxLevel;
    private String formula;
    private final Map<Integer, Double> levelOverrides;

    public MailmanJobConfiguration(File file) {
        super(file, "mailman.yml", "default/jobs/mailman.yml", true);

        blockWhitelist = new ArrayList<>();
        packages = new LinkedHashMap<>();
        levelOverrides = new LinkedHashMap<>();

        this.values();
    }

    @Override
    public void values() {
        this.enabled = config.getBoolean("enabled", true);
        this.name = config.getString("name", "<blue> Pieter Post");

        List<String> whitelistStrings = config.getStringList("whitelist");
        for (String matName : whitelistStrings) {
            Material material = Material.getMaterial(matName);
            if (material != null) blockWhitelist.add(material);
            else OpenGrinding.getInstance().getLogger().warning("Material " + matName + " does not exist!");
        }

        packages.clear();
        ConfigurationSection pickaxeSection = config.getConfigurationSection("packages");
        if (pickaxeSection != null) {
            for (String key : pickaxeSection.getKeys(false)) {
                ConfigurationSection section = pickaxeSection.getConfigurationSection(key);
                if (section != null) {
                    int level;
                    try {
                        level = Integer.parseInt(key);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        continue;
                    }
                    int waitTime = section.getInt("wait-time");

                    ConfigurationSection priceSection = section.getConfigurationSection("reward");
                    double cash = priceSection != null ? priceSection.getDouble("cash", 0.0) : 0.0;
                    double tokens = priceSection != null ? priceSection.getDouble("tokens", 0.0) : 0.0;

                    ConfigurationSection amountSection = section.getConfigurationSection("amount");
                    int min = amountSection != null ? amountSection.getInt("min", 0) : 0;
                    int max = amountSection != null ? amountSection.getInt("max", 0) : 0;

                    Package packace = new Package(
                            level,
                            waitTime,
                            new Price(cash, tokens),
                            new Amount(min, max)
                    );
                    packages.put(level, packace);
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
