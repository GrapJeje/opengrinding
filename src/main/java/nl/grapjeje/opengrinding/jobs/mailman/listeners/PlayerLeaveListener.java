package nl.grapjeje.opengrinding.jobs.mailman.listeners;

import nl.grapjeje.opengrinding.jobs.mailman.objects.MailmanJob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (!MailmanJob.isActive(player)) return;
        MailmanJob job = MailmanJob.getJobs().get(player.getUniqueId());
        if (job == null) return;
        job.stop(false);
    }
}
