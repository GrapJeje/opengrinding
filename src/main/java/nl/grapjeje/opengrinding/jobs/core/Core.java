package nl.grapjeje.opengrinding.jobs.core;

import nl.grapjeje.opengrinding.Opengrinding;
import nl.grapjeje.opengrinding.jobs.Job;
import nl.grapjeje.opengrinding.jobs.core.commands.OpenGrindingCommand;

public class Core extends Job {
    public Core() {
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
