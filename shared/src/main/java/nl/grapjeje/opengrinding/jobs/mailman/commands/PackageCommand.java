package nl.grapjeje.opengrinding.jobs.mailman.commands;

import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.jobs.mailman.objects.MailmanJob;
import org.bukkit.entity.Player;

public class PackageCommand implements Command {
    @Override
    public String getName() {
        return "pakketjes";
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getExecutor() instanceof Player player)) return;

        if (!MailmanJob.isActive(player)) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Jij bent niet bezig met post bezorgen!"));
            return;
        }
        MailmanJob.getJobs().get(player.getUniqueId()).sendRegionsList();
    }
}
