package nl.grapjeje.opengrinding;

import com.craftmend.storm.Storm;
import lombok.Getter;
import nl.grapjeje.core.Framework;
import nl.grapjeje.core.Main;
import nl.grapjeje.core.StormDatabase;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.fishing.FishingModule;
import nl.grapjeje.opengrinding.jobs.lumber.LumberModule;
import nl.grapjeje.opengrinding.jobs.mailman.MailmanModule;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.models.CurrencyModel;
import nl.grapjeje.opengrinding.models.FishLootTableModel;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import org.bukkit.Bukkit;
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

        if (Bukkit.getPluginManager().isPluginEnabled("OpenMinetopia")) {
            this.getLogger().info("Detected OpenMinetopia enabled. Waiting for Storm to initialize...");
            this.waitForStormReady();
        } else {
            this.getLogger().info("OpenMinetopia not found. Initializing with internal Storm...");
            Bukkit.getScheduler().runTask(this, () -> initializePlugin(null));
        }
    }

    private void registerModels() {
        framework.registerStormModel(PlayerGrindingModel::new);
        framework.registerStormModel(GrindingRegionModel::new);
        framework.registerStormModel(FishLootTableModel::new);
        framework.registerStormModel(CurrencyModel::new);
    }

    private void registerModules() {
        framework.registerModuleloader();
        framework.registerModule(CoreModule::new);
        framework.registerModule(MiningModule::new);
        framework.registerModule(FishingModule::new);
        framework.registerModule(LumberModule::new);
        framework.registerModule(MailmanModule::new);
    }

    private void waitForStormReady() {
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            int attempts = 0;
            final int MAX_ATTEMPTS = 100;
            @Override
            public void run() {
                attempts++;

                try {
                    var omStormDb = nl.openminetopia.modules.data.storm.StormDatabase.getInstance();
                    var omStorm = omStormDb.getStorm();
                    if (omStorm != null && isStormReady(omStorm)) {
                        getLogger().info("Detected OpenMinetopia Storm ready after " + attempts + " checks.");
                        Bukkit.getScheduler().cancelTasks(OpenGrinding.this);
                        initializePlugin(omStorm);
                        return;
                    }
                } catch (Throwable e) {
                    if (attempts % 20 == 0)
                        getLogger().info("Waiting for Storm... (" + attempts + " checks, error: " + e.getMessage() + ")");
                }

                if (attempts >= MAX_ATTEMPTS) {
                    getLogger().warning("⚠Storm did not initialize within 25 seconds. Falling back to internal Storm connection...");
                    Bukkit.getScheduler().cancelTasks(OpenGrinding.this);
                    initializePlugin(null);
                }
            }
        }, 0L, 5L);
    }

    private boolean isStormReady(Storm storm) {
        try {
            return storm.getDriver() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void initializePlugin(Storm storm) {
        try {
            framework.initializeStorm(storm);
            this.registerModels();

            this.registerModules();
            framework.getModuleLoader().enableModules();

            if (storm != null) this.getLogger().info("OpenGrinding fully initialized using OpenMinetopia Storm connection.");
            else this.getLogger().info("OpenGrinding initialized using internal Storm connection.");
        } catch (Exception e) {
            this.getLogger().severe("Failed to initialize OpenGrinding: " + e.getMessage());
            e.printStackTrace();
            this.getLogger().severe("Plugin will be disabled due to initialization failure.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (framework != null && framework.getModuleLoader() != null)
            framework.getModuleLoader().disableModules();
        if (!StormDatabase.getInstance().isUsingExternalStorm() && Main.getDb() != null)
            Main.getDb().close();
    }
}