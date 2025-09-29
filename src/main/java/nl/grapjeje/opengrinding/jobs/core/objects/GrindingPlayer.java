package nl.grapjeje.opengrinding.jobs.core.objects;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class GrindingPlayer {
    private final static List<GrindingPlayer> players = new ArrayList<>();

    private final OfflinePlayer player; // TODO: Replace offlineplayer for minetopiaplayer
    private final UUID uuid; // TODO: Remove when MinetopiaPlayer is here

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
}
