package nl.grapjeje.opengrinding.jobs.fishing.base.listeners;

import nl.grapjeje.opengrinding.jobs.fishing.base.games.FishingGame;
import nl.grapjeje.opengrinding.jobs.fishing.base.games.MazeGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerCatchListener implements Listener {

    private final List<FishingGame> games = List.of(
            new MazeGame()
    );

    @EventHandler
    public void onCatch(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        Player player = e.getPlayer();

        e.setCancelled(true);
        FishingGame game = this.getRandomGame();
        game.start(player);
    }

    private FishingGame getRandomGame() {
        ArrayList<FishingGame> list = new ArrayList<>(games);
        Collections.shuffle(list);
        return list.getFirst();
    }
}
