package nl.grapjeje.opengrinding.jobs.fishing.base.games;

import lombok.Getter;
import nl.grapjeje.opengrinding.OpenGrinding;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

@Getter
public abstract class FishingGame {
    @Getter
    private static Map<UUID, FishingGame> playersInGame = new ConcurrentHashMap<>();

    protected Player player;

    private final BukkitTask tickTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
            OpenGrinding.getInstance(),
            this::tick,
            1L,
            1L
    );

    public void start(@NotNull Player player) {
        this.player = player;
        playersInGame.put(player.getUniqueId(), this);
        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), this::visualize);
    }

    public void tick() {
        if (Math.random() < 0.001)
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> stop(false));
    }

    public abstract void visualize();

    public void stop(boolean completed) {
        playersInGame.remove(player.getUniqueId());
        tickTask.cancel();
    }

    public static boolean isInGame(Player player) {
        return playersInGame.containsKey(player.getUniqueId());
    }
}

