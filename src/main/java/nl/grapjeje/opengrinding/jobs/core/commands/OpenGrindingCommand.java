package nl.grapjeje.opengrinding.jobs.core.commands;

import nl.grapjeje.core.command.CreditCommand;
import nl.grapjeje.opengrinding.Opengrinding;

public class OpenGrindingCommand implements CreditCommand {

    @Override
    public String getName() {
        return "opengrinding";
    }

    @Override
    public String getColor() {
        return "<red>";
    }

    @Override
    public String getDescription() {
        return "<gray>Een grinding plugin voor OpenMinetopia";
    }

    @Override
    public String getVersion() {
        return Opengrinding.getInstance().getDescription().getVersion();
    }
}
