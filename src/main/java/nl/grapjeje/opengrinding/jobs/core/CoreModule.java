package nl.grapjeje.opengrinding.jobs.core;

import nl.grapjeje.core.modules.Module;
import nl.grapjeje.opengrinding.Opengrinding;
import nl.grapjeje.opengrinding.jobs.core.commands.OpenGrindingCommand;
import nl.grapjeje.opengrinding.jobs.core.objects.Region;

import java.util.ArrayList;
import java.util.List;

public class CoreModule extends Module {

    private List<Region> regions = new ArrayList<>();

    public CoreModule() {
        super("core");
    }

    @Override
    protected void onEnable() {
        Opengrinding.getFramework().registerCommand(OpenGrindingCommand::new);
    }

    @Override
    protected void onDisable() {

    }
}
