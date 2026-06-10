/*
 * WARNING:
 * This command (/opengrinding) is part of the OpenGrinding plugin and is
 * covered by the custom MIT-based license.
 * Before making any modifications to this command, you MUST read and fully
 * understand the license terms.
 * Any redistribution or changes must comply with the license requirements,
 * including keeping the command publicly executable and preserving the
 * attribution to "GrapJeje".
 */

package nl.grapjeje.opengrinding.core.commands;

import nl.grapjeje.core.Framework;
import nl.grapjeje.core.command.CreditCommand;
import nl.grapjeje.opengrinding.OpenGrinding;

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
        return OpenGrinding.getInstance().getDescription().getVersion();
    }

    @Override
    public Framework getFramework() {
        return OpenGrinding.getFramework();
    }
}
