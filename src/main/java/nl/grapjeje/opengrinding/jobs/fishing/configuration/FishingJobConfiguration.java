package nl.grapjeje.opengrinding.jobs.fishing.configuration;

import lombok.Getter;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;
import nl.grapjeje.opengrinding.utils.configuration.ShopConfig;
import nl.grapjeje.opengrinding.utils.currency.Price;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class FishingJobConfiguration extends JobConfig implements ShopConfig {
    public record Fish(String name, Price price) {}
    public record Rod(String name, Price price) {}

    private boolean gamesEnabled;

    private boolean sellEnabled;
    private boolean openBuyShop;
    private boolean buyEnabled;

    private final Map<String, Fish> fishes;
    private final Map<String, Rod> rods;

    public FishingJobConfiguration(File file) {
        super(file, "jobs", "fishing.yml", "default/jobs/fishing.yml", true);

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
                    ConfigurationSection priceSection = section.getConfigurationSection("sell-price");
                    double cash = priceSection != null ? priceSection.getDouble("cash", 0.0) : 0.0;
                    double tokens = priceSection != null ? priceSection.getDouble("tokens", 0.0) : 0.0;

                    try {
                        Fish fish = new Fish(
                                key,
                                new Price(cash, tokens)
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
                    ConfigurationSection priceSection = section.getConfigurationSection("price");
                    double cash = priceSection != null ? priceSection.getDouble("cash", 0.0) : 0.0;
                    double tokens = priceSection != null ? priceSection.getDouble("tokens", 0.0) : 0.0;

                    try {
                        Rod rod = new Rod(
                                key,
                                new Price(cash, tokens)
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
