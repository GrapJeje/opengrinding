package nl.grapjeje.opengrinding.jobs.fishing.base.games;

import lombok.Getter;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import nl.grapjeje.opengrinding.jobs.fishing.base.events.PlayerFishCatchEvent;
import nl.grapjeje.opengrinding.jobs.fishing.base.listeners.PlayerCatchListener;
import nl.grapjeje.opengrinding.utils.Menu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

@Getter
public abstract class FishingGame extends Menu {
    @Getter
    private static Map<UUID, FishingGame> playersInGame = new ConcurrentHashMap<>();

    protected Player player;

    private final BukkitTask tickTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
            OpenGrinding.getInstance(),
            this::tick,
            1L,
            1L
    );

    @Override
    public void open(Player player) {
        this.start(player);
    }

    public void start(@NotNull Player player) {
        this.player = player;
        playersInGame.put(player.getUniqueId(), this);
        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), this::visualize);
    }

    public void tick() {
        if (Math.random() < 0.001)
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> this.stop(false));
    }

    public abstract void visualize();

    public void stop(boolean completed) {
        if (completed) {
            ItemStack item = PlayerCatchListener.getPlayerLoot().get(player.getUniqueId());
            if (item != null) {
                player.getInventory().addItem(item);
                GrindingPlayer gp = new GrindingPlayer(player.getUniqueId(), GrindingPlayer.loadOrCreatePlayerModel(player, Jobs.FISHING));
                new PlayerFishCatchEvent(gp, item).callEvent();
            }
        }
        playersInGame.remove(player.getUniqueId());
        tickTask.cancel();
    }

    public static boolean isInGame(Player player) {
        return playersInGame.containsKey(player.getUniqueId());
    }
}

