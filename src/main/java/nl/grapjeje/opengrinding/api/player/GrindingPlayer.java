package nl.grapjeje.opengrinding.api.player;

import nl.grapjeje.opengrinding.api.Jobs;
import nl.grapjeje.opengrinding.core.objects.CraftGrindingPlayer;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public interface GrindingPlayer {

    MinetopiaPlayer getPlayer();

    Player getBukkitPlayer();

    CompletableFuture<Void> save(Jobs job);

    void addProgress(Jobs job, double xp);

    boolean isInRegionWithJob(Jobs job);

    boolean isInventoryFull();

    static CompletableFuture<PlayerGrindingModel> loadOrCreatePlayerModelAsync(Player player, Jobs job) {
        return CraftGrindingPlayer.loadOrCreatePlayerModelAsync(player, job);
    }
}
