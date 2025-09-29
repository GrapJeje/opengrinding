package nl.grapjeje.opengrinding.jobs.core.objects;

import lombok.Getter;
import lombok.Setter;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.events.PlayerLevelChangeEvent;
import nl.grapjeje.opengrinding.jobs.core.events.PlayerValueChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class GrindingPlayer {
    private final static List<GrindingPlayer> players = new ArrayList<>();

    private final OfflinePlayer player; // TODO: Replace offlineplayer for minetopiaplayer
    private final UUID uuid; // TODO: Remove when MinetopiaPlayer is here

    private final Map<Jobs, JobProgress> jobProgressMap = new EnumMap<>(Jobs.class);

    public GrindingPlayer(UUID uuid) {
        this.uuid = uuid;
        this.player = Bukkit.getOfflinePlayer(uuid);

        players.add(this);
    }

    public static GrindingPlayer get(Player player) {
        return players.stream()
                .filter(gp -> gp.getUuid().equals(player.getUniqueId()))
                .findFirst()
                .orElse(new GrindingPlayer(player.getUniqueId()));
    }

    /* ---------- Level ---------- */

    public JobProgress getProgress(Jobs job) {
        return jobProgressMap.get(job);
    }

    public JobProgress addProgress(Jobs job, double xp) {
        JobProgress progress = jobProgressMap.get(job);
        double newXp = progress.getXp() + xp;

        new PlayerValueChangeEvent(this, job, progress.getXp(), newXp).callEvent(); //TODO: Check if you want to reset xp on level up

        int newLevel = progress.getLevel() + 1; // TODO: Check for max level
        // TODO: Add level up logic
        if (true) new PlayerLevelChangeEvent(this, job, progress.getLevel(), newLevel).callEvent();

        progress.setLevel(newLevel);
        progress.setXp(newXp);
        return progress;
    }

    public void setProgress(Jobs job, int level, double xp) {
        jobProgressMap.put(job, new JobProgress(level, xp));
    }
}
