package nl.grapjeje.opengrinding.jobs.mailman.listeners;

import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.api.GrindingRegion;
import nl.grapjeje.opengrinding.api.Jobs;
import nl.grapjeje.opengrinding.api.player.GrindingPlayer;
import nl.grapjeje.opengrinding.core.objects.CraftGrindingPlayer;
import nl.grapjeje.opengrinding.jobs.mailman.MailmanModule;
import nl.grapjeje.opengrinding.api.player.events.mailman.PlayerDeliverPackageEvent;
import nl.grapjeje.opengrinding.jobs.mailman.objects.MailmanJob;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class InteractionListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        if (block == null) return;

        if (!MailmanJob.isActive(player)) return;
        if (!MailmanModule.getConfig().getBlockWhitelist().contains(block.getType())) return;

        List<String> values = MailmanJob.getJobs().get(player.getUniqueId()).getPlayerRouteValues().get(player.getUniqueId());
        if (values == null) return;

        GrindingRegion region = GrindingRegion.getRegionAt(block.getLocation());
        if (!values.contains(region.getValue())) return;

        e.setCancelled(true);

        GrindingPlayer.loadOrCreatePlayerModelAsync(player, Jobs.MAILMAN)
                .thenAccept(model ->
                        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                            GrindingPlayer gp = CraftGrindingPlayer.get(player.getUniqueId(), model);
                    PlayerDeliverPackageEvent deliverEvent = new PlayerDeliverPackageEvent(gp, region);
                    deliverEvent.callEvent();
                }));
    }
}
