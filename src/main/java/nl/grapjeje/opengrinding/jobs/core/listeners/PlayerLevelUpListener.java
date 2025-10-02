package nl.grapjeje.opengrinding.jobs.core.listeners;

import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.events.PlayerLevelChangeEvent;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerLevelUpListener implements Listener {

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {
        int oldLvl = e.getOldLevel();
        int newLvl = e.getNewLevel();
        if (oldLvl >= newLvl) return;

        GrindingPlayer gp = e.getPlayer();
        Player player = gp.getPlayer().getBukkit().getPlayer();
        if (player == null) return;
        player.sendMessage(MessageUtil.filterMessage("<green>Level up! " + this.getJobName(e.getJob()) + " level <bold>"
                + oldLvl + "<!bold> -> <bold>" + newLvl + "</bold>!"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 0.5F);
    }

    String getJobName(Jobs job) {
        return switch (job) {
            case MINING -> "Mine";
            default -> "";
        };
    }
}
