package nl.grapjeje.opengrinding.jobs.core;

import nl.grapjeje.core.modules.Module;
import nl.grapjeje.opengrinding.Opengrinding;
import nl.grapjeje.opengrinding.jobs.core.commands.OpenGrindingCommand;

public class CoreModule extends Module {
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
