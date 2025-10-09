package nl.grapjeje.opengrinding;

import com.craftmend.storm.Storm;
import lombok.Getter;
import nl.grapjeje.core.Framework;
import nl.grapjeje.core.Main;
import nl.grapjeje.core.StormDatabase;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.fishing.FishingModule;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.models.FishLootTableModel;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import org.bukkit.plugin.java.JavaPlugin;

public final class OpenGrinding extends JavaPlugin {

    @Getter
    private static OpenGrinding instance;
    @Getter
    private static Framework framework;

    @Override
    public void onEnable() {
        instance = this;

        framework = Framework.init(this);

        // Initialize Storm - framework will automatically use OpenMinetopia's if available
        // If OpenMinetopia is not present, it will use null (you can provide your own Storm here)
        framework.initializeStorm(null);

        // Register models via framework
        this.registerStormModels();

        framework.registerModuleloader();
        framework.registerModule(CoreModule::new);
        framework.registerModule(MiningModule::new);
        framework.registerModule(FishingModule::new);

        framework.getModuleLoader().enableModules();
    }

    @Override
    public void onDisable() {
        framework.getModuleLoader().disableModules();

        // Only close our own Storm connection if we're not using OpenMinetopia's
        if (!StormDatabase.getInstance().isUsingExternalStorm() && Main.getDb() != null) {
            Main.getDb().close();
        }
    }

    private void registerStormModels() {
        // Use framework's registerStormModel with Supplier pattern
        framework.registerStormModel(PlayerGrindingModel::new);
        framework.registerStormModel(GrindingRegionModel::new);
        framework.registerStormModel(FishLootTableModel::new);
    }
}