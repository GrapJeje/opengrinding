package nl.grapjeje.opengrinding.jobs.fishing.base.listeners;

import nl.grapjeje.opengrinding.jobs.fishing.base.games.FishingGame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        FishingGame.getPlayersInGame().remove(e.getPlayer().getUniqueId());
    }
}
