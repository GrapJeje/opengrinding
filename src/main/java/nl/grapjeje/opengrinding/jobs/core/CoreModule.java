package nl.grapjeje.opengrinding.jobs.core;

import lombok.Getter;
import nl.grapjeje.core.modules.Module;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.core.commands.GrindingRegionCommand;
import nl.grapjeje.opengrinding.jobs.core.commands.OpenGrindingCommand;
import nl.grapjeje.opengrinding.jobs.core.configuration.GrindingLevelsConfiguration;
import nl.grapjeje.opengrinding.jobs.core.listeners.PlayerRegionWandListener;

public class CoreModule extends Module {
    @Getter
    private static GrindingLevelsConfiguration grindingLevelsConfiguration;

    public CoreModule() {
        super("core");
    }

    @Override
    protected void onEnable() {
        grindingLevelsConfiguration = new GrindingLevelsConfiguration(OpenGrinding.getInstance().getDataFolder());

        OpenGrinding.getFramework().registerCommand(OpenGrindingCommand::new);
        OpenGrinding.getFramework().registerCommand(GrindingRegionCommand::new);

        OpenGrinding.getFramework().registerListener(PlayerRegionWandListener::new);
    }

    @Override
    protected void onDisable() {

    }
}
