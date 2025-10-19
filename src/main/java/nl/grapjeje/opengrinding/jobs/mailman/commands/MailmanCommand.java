package nl.grapjeje.opengrinding.jobs.mailman.commands;

import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.opengrinding.jobs.mailman.guis.MailmanGui;
import org.bukkit.entity.Player;

public class MailmanCommand implements Command {
    @Override
    public String getName() {
        return "mailman";
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getExecutor() instanceof Player player)) return;
        if (!player.hasPermission("opengrinding.*")) return;

        if (args.length == 0)
            new MailmanGui().open(player);
        else new MailmanGui(String.join(" ", args)).open(player);
    }
}
