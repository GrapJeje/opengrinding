package nl.grapjeje.opengrinding.jobs.core.configuration;

import lombok.Getter;
import nl.grapjeje.core.Config;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.*;

@Getter
public class GrindingLevelsConfiguration extends Config {

    private final int maxLevel;
    private final int oreUnlockInterval;
    private final String formula;
    private final Map<String, Integer> pointsPerOre = new LinkedHashMap<>();
    private final Map<Integer, Integer> overrides = new HashMap<>();

    public GrindingLevelsConfiguration(File file) {
        super(file, "grindinglevels.yml", "default/grindinglevels.yml", true);

        ConfigurationSection jobsSection = config.getConfigurationSection("jobs");
        if (jobsSection == null) throw new IllegalStateException("No 'jobs' section found in grindinglevels.yml");

        ConfigurationSection mining = jobsSection.getConfigurationSection("mining");
        if (mining == null) throw new IllegalStateException("No 'mining' section found in grindinglevels.yml");

        this.maxLevel = mining.getInt("max-level", 100);
        this.oreUnlockInterval = mining.getInt("ore-unlock-interval", 5);
        this.formula = mining.getString("formula", "250 + (<level> - 50) * 250");

        for (String key : mining.getKeys(false)) {
            if (key.startsWith("points-per-"))
                pointsPerOre.put(key.replace("points-per-", ""), mining.getInt(key));
        }

        ConfigurationSection overridesSection = mining.getConfigurationSection("overrides");
        if (overridesSection != null) {
            for (String key : overridesSection.getKeys(false)) {
                int level = Integer.parseInt(key);
                int value = overridesSection.getInt(key);
                overrides.put(level, value);
            }
        }
    }

    public int getRequiredLevelForOre(String oreName) {
        List<String> ores = new ArrayList<>(this.getPointsPerOre().keySet());
        return ores.indexOf(oreName.toLowerCase()) * this.getOreUnlockInterval();
    }
}
