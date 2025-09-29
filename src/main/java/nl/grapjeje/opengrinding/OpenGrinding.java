package nl.grapjeje.opengrinding;

import lombok.Getter;
import nl.grapjeje.core.Framework;
import nl.grapjeje.core.Main;
import nl.grapjeje.core.modules.ModuleLoader;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class OpenGrinding extends JavaPlugin {

    @Getter
    private static OpenGrinding instance;
    @Getter
    private static Framework framework;
    @Getter
    private static ModuleLoader moduleLoader;

    @Override
    public void onEnable() {
        instance = this;

        framework = Framework.init(this);

        framework.registerModule(CoreModule::new);
        framework.registerModule(MiningModule::new);

        moduleLoader = new ModuleLoader();
    }

    @Override
    public void onDisable() {
        moduleLoader.disableModules();
        if (Main.getDb() != null) Main.getDb().close();
    }
}
