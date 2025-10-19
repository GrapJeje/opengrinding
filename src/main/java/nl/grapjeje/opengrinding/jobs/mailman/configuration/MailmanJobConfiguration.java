package nl.grapjeje.opengrinding.jobs.mailman.configuration;

import lombok.Getter;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;
import nl.grapjeje.opengrinding.utils.configuration.LevelConfig;
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
    private final List<Material> blockWhitelist;
    private int waitTimeInMinutes;

    private int maxLevel;
    private String formula;
    private final Map<Integer, Double> levelOverrides;

    public MailmanJobConfiguration(File file) {
        super(file, "mailman.yml", "default/jobs/mailman.yml", true);

        levelOverrides = new LinkedHashMap<>();
        blockWhitelist = new ArrayList<>();

        this.values();
    }

    @Override
    public void values() {
        this.enabled = config.getBoolean("enabled", true);
        this.waitTimeInMinutes = config.getInt("wait-time", 1);

        ConfigurationSection whitelistSection = config.getConfigurationSection("whitelist");
        if (whitelistSection != null) {
            for (String key : whitelistSection.getKeys(false)) {
                Material material = Material.getMaterial(whitelistSection.getString(key));
                if (material != null) blockWhitelist.add(material);
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

        xpExecutor.submit(() -> {
            try {
                Expression expression = new ExpressionBuilder(formula)
                        .variable("level")
                        .build()
                        .setVariable("level", level);

                double value = expression.evaluate();
                xpCache.put(level, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return 0.0;
    }

    @Override
    public Double getLevelOverride(int level) {
        return levelOverrides.getOrDefault(level, null);
    }
}
