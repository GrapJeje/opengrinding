package nl.grapjeje.opengrinding.jobs.fishing.base.listeners;

import nl.grapjeje.opengrinding.jobs.fishing.base.events.PlayerFishCatchEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class PlayerCatchListener implements Listener {

    @EventHandler
    public void onCatch(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        e.setCancelled(true);

        new PlayerFishCatchEvent(null, null).callEvent();
    }
}
