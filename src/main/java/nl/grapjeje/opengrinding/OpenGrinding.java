package nl.grapjeje.opengrinding;

import com.craftmend.storm.Storm;
import com.craftmend.storm.api.StormModel;
import com.craftmend.storm.connection.hikaricp.HikariDriver;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import nl.grapjeje.core.Framework;
import nl.grapjeje.core.Main;
import nl.grapjeje.core.gui.GuiImpl;
import nl.grapjeje.core.modules.ModuleLoader;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
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
        this.registerStormModels();

        framework.registerModuleloader();
        framework.registerModule(CoreModule::new);
        framework.registerModule(MiningModule::new);

        framework.getModuleLoader().enableModules();
    }

    @Override
    public void onDisable() {
        framework.getModuleLoader().disableModules();
        if (Main.getDb() != null) Main.getDb().close();
    }

    private void registerStormModels() {
        this.registerStormModel(new PlayerGrindingModel());
        this.registerStormModel(new GrindingRegionModel());
    }

    @SneakyThrows
    private void registerStormModel(StormModel model) {
        Storm storm = StormDatabase.getInstance().getStorm();

        storm.registerModel(model);
        storm.runMigrations();
    }
}
