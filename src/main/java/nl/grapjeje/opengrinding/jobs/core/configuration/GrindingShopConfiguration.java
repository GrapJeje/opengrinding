package nl.grapjeje.opengrinding.jobs.core.configuration;

import lombok.Getter;
import nl.grapjeje.core.Config;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Getter
public class GrindingShopConfiguration extends Config {

    private final Map<String, Shop> shops = new HashMap<>();

    public GrindingShopConfiguration(File file) {
        super(file, "grindingshop.yml", "default/grindingshop.yml", true);

        ConfigurationSection shopsSection = config.getConfigurationSection("shops");
        if (shopsSection == null) return;

        for (String shopName : shopsSection.getKeys(false)) {
            ConfigurationSection shopSection = shopsSection.getConfigurationSection(shopName);
            if (shopSection == null) continue;

            // Sell
            Map<String, Double> sell = new HashMap<>();
            boolean sellEnabled = true;
            ConfigurationSection sellSection = shopSection.getConfigurationSection("sell");
            if (sellSection != null) {
                sellEnabled = sellSection.getBoolean("enabled", true);
                for (String item : sellSection.getKeys(false)) {
                    if (!item.equalsIgnoreCase("enabled")) {
                        sell.put(item.toLowerCase(), sellSection.getDouble(item, 0));
                    }
                }
            }

            // Buy
            Map<String, Map<String, Double>> buy = new HashMap<>();
            boolean buyEnabled = true;
            ConfigurationSection buySection = shopSection.getConfigurationSection("buy");
            if (buySection != null) {
                buyEnabled = buySection.getBoolean("enabled", true);
                for (String category : buySection.getKeys(false)) {
                    if (category.equalsIgnoreCase("enabled")) continue;

                    Map<String, Double> categoryItems = new HashMap<>();
                    ConfigurationSection categorySection = buySection.getConfigurationSection(category);
                    if (categorySection != null) {
                        for (String item : categorySection.getKeys(false)) {
                            categoryItems.put(item.toLowerCase(), categorySection.getDouble(item, 0));
                        }
                    }
                    buy.put(category.toLowerCase(), categoryItems);
                }
            }

            shops.put(shopName.toLowerCase(), new Shop(shopName, sellEnabled, sell, buyEnabled, buy));
        }
    }

    public record Shop(
            String name,
            boolean sellEnabled,
            Map<String, Double> sell,
            boolean buyEnabled,
            Map<String, Map<String, Double>> buy
    ) {}
}
