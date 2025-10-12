package nl.grapjeje.opengrinding.jobs.fishing.configuration;

import lombok.Getter;
import nl.grapjeje.core.Config;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration;
import nl.grapjeje.opengrinding.jobs.mining.objects.Ore;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;
import nl.grapjeje.opengrinding.utils.configuration.ShopConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class FishingJobConfiguration extends JobConfig implements ShopConfig {
    public record Fish(String name, double sellPrice) {}
    public record Rod(String name, double buyPrice) {}

    private boolean gamesEnabled;

    private boolean sellEnabled;
    private boolean openBuyShop;
    private boolean buyEnabled;

    private final Map<String, Fish> fishes;
    private final Map<String, Rod> rods;

    public FishingJobConfiguration(File file) {
        super(file, "fishing.yml", "default/fishing.yml", true);

        fishes = new LinkedHashMap<>();
        rods = new LinkedHashMap<>();

        this.values();
    }

    @Override
    public void values() {
        this.enabled = config.getBoolean("enabled", true);
        this.gamesEnabled = config.getBoolean("enable-games", true);

        ConfigurationSection sellSection = config.getConfigurationSection("economy.sell");
        this.sellEnabled = sellSection != null && sellSection.getBoolean("enabled", true);

        ConfigurationSection openBuy = config.getConfigurationSection("economy.sell");
        this.openBuyShop = openBuy != null && openBuy.getBoolean("open-buy-shop", true);

        ConfigurationSection buySection = config.getConfigurationSection("economy.buy");
        this.buyEnabled = buySection != null && buySection.getBoolean("enabled", true);

        fishes.clear();
        ConfigurationSection fishSection = config.getConfigurationSection("fishes");
        if (fishSection != null) {
            for (String key : fishSection.getKeys(false)) {
                ConfigurationSection section = fishSection.getConfigurationSection(key);
                if (section != null) {
                    try {
                        Fish fish = new Fish(
                                key,
                                section.getDouble("sell-price")
                        );
                        fishes.put(key, fish);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }

        rods.clear();
        ConfigurationSection rodSection = config.getConfigurationSection("rods");
        if (rodSection != null) {
            for (String key : rodSection.getKeys(false)) {
                ConfigurationSection section = rodSection.getConfigurationSection(key);
                if (section != null) {
                    try {
                        Rod rod = new Rod(
                                key,
                                section.getDouble("buy-price")
                        );
                        rods.put(key, rod);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }
    }

    public Fish getFish(Material material) {
        return fishes.get(material.name().toLowerCase());
    }
}
