package nl.grapjeje.opengrinding.jobs.farming.listeners;

import nl.grapjeje.opengrinding.api.GrindingRegion;
import nl.grapjeje.opengrinding.api.Jobs;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class TrampleListener implements Listener {

    @EventHandler
    public void onTrample(PlayerInteractEvent e) {
        if (e.getAction() != Action.PHYSICAL) return;
        Block clicked = e.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.FARMLAND) return;

        if (GrindingRegion.isInRegionWithJobSync(clicked.getLocation(), Jobs.FARMING))
            e.setCancelled(true);
    }
}
