package nl.grapjeje.opengrinding.jobs.fishing.listeners;

import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.fishing.games.FishingGame;
import nl.grapjeje.opengrinding.jobs.fishing.objects.FishLootTable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerSessionListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(OpenGrinding.getInstance(),
                () -> FishLootTable.checkChancesPerValue().thenAccept(map -> map.forEach((value, isValid) -> {
                    if (!isValid) player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Fishing loot value <bold>" + value + "<!bold> heeft geen totale kans van 100%!"));
                })), 30L );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        FishingGame.getPlayersInGame().remove(e.getPlayer().getUniqueId());
    }
}
