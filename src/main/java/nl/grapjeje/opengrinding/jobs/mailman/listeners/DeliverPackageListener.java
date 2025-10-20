package nl.grapjeje.opengrinding.jobs.mailman.listeners;

import nl.grapjeje.opengrinding.jobs.mailman.events.DeliverPackageEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.UUID;

public class DeliverPackageListener implements Listener {

    @EventHandler
    public void onPackageDeliver(DeliverPackageEvent e) {
        Player player = e.getPlayer().getPlayer().getBukkit().getPlayer();
        if (player == null) return;

        UUID uuid = player.getUniqueId();
        List<String> values = e.getJob().getPlayerRouteValues().get(uuid);
        if (values == null) return;

        if (!values.contains(e.getRegion().getValue())) return;
        values.remove(e.getRegion().getValue());
        e.getJob().getPlayerRouteValues().put(uuid, values);

        if (values.isEmpty()) {
            e.getJob().getPlayerRouteValues().remove(uuid);
            e.getJob().setCompleted(true);
        } else {
            if (player.getInventory().getItemInOffHand().getAmount() > 0)
                player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
            e.getJob().sendRegionsList();
        }
    }
}
