package nl.grapjeje.opengrinding.jobs.mailman.commands;

import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.jobs.mailman.guis.MailmanGui;
import nl.grapjeje.opengrinding.jobs.mailman.objects.MailmanJob;
import org.bukkit.Sound;
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

        if (MailmanJob.isActive(player)) {
            MailmanJob job = MailmanJob.getJobs().get(player.getUniqueId());
            if (job.isCompleted()) {
                job.stop(true);
                return;
            } else {
                player.sendMessage(MessageUtil.filterMessage(
                        "<warning>⚠ Jij bent nog met een route bezig! Voer <bold>/pakketjes<!bold> uit om je route te bekijken!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }

        if (args.length == 0)
            new MailmanGui().open(player);
        else new MailmanGui(String.join(" ", args)).open(player);
    }
}
