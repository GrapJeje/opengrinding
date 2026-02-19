package nl.grapjeje.opengrinding.core.commands;

import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.core.CoreModule;
import nl.grapjeje.opengrinding.core.guis.JobsMenu;
import org.bukkit.entity.Player;

public class JobsCommand implements Command {

    @Override
    public String getName() {
        return "jobs";
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        Player player = source.getPlayer();
        if (player == null) {
            source.getSender().sendRichMessage("<warning>⚠ Alleen spelers kunnen dit commando gebruiken!");
            return;
        }
        if (!CoreModule.getConfig().isJobsCommand()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Het Jobs commando staat momenteel uit!"));
            return;
        }

        new JobsMenu().open(player);
    }
}
