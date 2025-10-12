package nl.grapjeje.opengrinding.jobs.core.configuration;

import lombok.Getter;
import nl.grapjeje.core.Config;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

@Getter
public class DefaultConfiguration extends Config {
    private boolean jobSkullsOnPlayerhead;

    private boolean buyInTokens;
    private boolean sellInTokens;
    private boolean dailyLimit;

    private double cashLimit;
    private double tokenLimit;

    public DefaultConfiguration(File file) {
        super(file, "config.yml", "default/config.yml", true);
    }

    @Override
    public void values() {
        this.jobSkullsOnPlayerhead = config.getBoolean("jobskullsonplayerhead", false);

        ConfigurationSection tokenSection = config.getConfigurationSection("currency");
        this.buyInTokens = tokenSection != null && tokenSection.getBoolean("buy-in-tokens", false);
        this.sellInTokens = tokenSection != null && tokenSection.getBoolean("sell-in-tokens", true);

        ConfigurationSection dailyLimitSection = tokenSection.getConfigurationSection("daily-limit");
        this.dailyLimit = dailyLimitSection != null && dailyLimitSection.getBoolean("enabled", false);

        ConfigurationSection amountSection = dailyLimitSection.getConfigurationSection("amount");
        this.cashLimit = amountSection != null ? amountSection.getDouble("cash", 10000.0) : 10000.0;
        this.tokenLimit = amountSection != null ? amountSection.getDouble("tokens", 30.0) : 30.0;
    }
}
