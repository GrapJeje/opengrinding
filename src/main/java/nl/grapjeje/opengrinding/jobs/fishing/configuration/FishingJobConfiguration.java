package nl.grapjeje.opengrinding.jobs.fishing.configuration;

import lombok.Getter;
import nl.grapjeje.core.Config;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;
import nl.grapjeje.opengrinding.utils.configuration.ShopConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

@Getter
public class FishingJobConfiguration extends JobConfig implements ShopConfig {
    private boolean gamesEnabled;

    private boolean sellEnabled;
    private boolean openBuyShop;
    private boolean buyEnabled;

    public FishingJobConfiguration(File file) {
        super(file, "fishing.yml", "default/fishing.yml", true);

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
    }
}
