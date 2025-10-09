package nl.grapjeje.opengrinding.jobs.fishing.base.listeners;

import nl.grapjeje.opengrinding.jobs.fishing.base.games.FishingGame;
import nl.grapjeje.opengrinding.jobs.fishing.base.games.MazeGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.List;
import java.util.Random;

public class PlayerCatchListener implements Listener {

    private final List<Class<? extends FishingGame>> gameTypes = List.of(
            MazeGame.class
    );

    private final Random random = new Random();

    @EventHandler
    public void onCatch(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        e.setCancelled(true);

        Player player = e.getPlayer();
        e.getHook().remove();

        FishingGame game = createRandomGame();
        if (game != null) {
            game.start(player);
        }
    }

    private FishingGame createRandomGame() {
        if (gameTypes.isEmpty()) return null;
        Class<? extends FishingGame> gameClass = gameTypes.get(random.nextInt(gameTypes.size()));

        try {
            return gameClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
