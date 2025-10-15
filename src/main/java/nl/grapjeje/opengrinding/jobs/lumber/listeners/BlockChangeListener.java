package nl.grapjeje.opengrinding.jobs.lumber.listeners;

import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingRegion;
import nl.grapjeje.opengrinding.jobs.lumber.LumberModule;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BlockChangeListener implements Listener {

    @EventHandler
    public void onChange(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        LumberModule lumberModule = OpenGrinding.getFramework().getModuleLoader()
                .getModules().stream()
                .filter(m -> m instanceof LumberModule)
                .map(m -> (LumberModule) m)
                .findFirst()
                .orElse(null);

        if (lumberModule == null || lumberModule.isDisabled()) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        if (GrindingRegion.isInRegionWithJob(e.getBlock().getLocation(), Jobs.LUMBER))
            e.setCancelled(true);
    }
}
