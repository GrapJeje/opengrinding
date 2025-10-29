package nl.grapjeje.opengrinding.jobs.core.listeners;

import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.events.PlayerLevelChangeEvent;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class PlayerLevelUpListener implements Listener {
    private record PlayerJob(UUID playerId, Jobs job, Integer oldLevel, Integer newLevel) {}

    private final Map<PlayerJob, Long> levelUpCooldowns = new HashMap<>();

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {
        int oldLvl = e.getOldLevel();
        int newLvl = e.getNewLevel();
        if (oldLvl >= newLvl) return;

        GrindingPlayer gp = e.getPlayer();
        Player player = gp.getPlayer().getBukkit().getPlayer();
        if (player == null) return;

        PlayerJob key = new PlayerJob(player.getUniqueId(), e.getJob(), oldLvl, newLvl);
        long now = System.currentTimeMillis();

        long COOLDOWN_MS = 1000;
        if (levelUpCooldowns.containsKey(key) && now - levelUpCooldowns.get(key) < COOLDOWN_MS) return;
        levelUpCooldowns.put(key, now);

        player.sendMessage(MessageUtil.filterMessage("<green>Level up! " + getJobName(e.getJob()) + " level <bold>"
                + oldLvl + "<!bold> -> <bold>" + newLvl + "</bold>!"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 0.5F);
    }

    private String getJobName(Jobs job) {
        return switch (job) {
            case MINING -> "Mine";
            case LUMBER -> "Lumber";
            case MAILMAN -> "Postbode";
            default -> "";
        };
    }
}
