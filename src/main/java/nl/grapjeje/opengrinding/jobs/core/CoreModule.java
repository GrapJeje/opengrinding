package nl.grapjeje.opengrinding.jobs.core;

import lombok.Getter;
import nl.grapjeje.core.modules.Module;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.core.commands.OpenGrindingCommand;
import nl.grapjeje.opengrinding.jobs.core.configuration.GrindingLevelsConfiguration;
import nl.grapjeje.opengrinding.jobs.core.objects.Region;

import java.util.ArrayList;
import java.util.List;

public class CoreModule extends Module {

    private List<Region> regions = new ArrayList<>();

    @Getter
    private static GrindingLevelsConfiguration grindingLevelsConfiguration;

    public CoreModule() {
        super("core");
    }

    @Override
    protected void onEnable() {
        grindingLevelsConfiguration = new GrindingLevelsConfiguration(OpenGrinding.getInstance().getDataFolder());

        OpenGrinding.getFramework().registerCommand(OpenGrindingCommand::new);
    }

    @Override
    protected void onDisable() {

    }
}
