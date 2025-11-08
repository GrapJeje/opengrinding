package nl.grapjeje.opengrinding.jobs.fishing.listeners;

import lombok.Getter;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.api.GrindingRegion;
import nl.grapjeje.opengrinding.api.Jobs;
import nl.grapjeje.opengrinding.api.player.GrindingPlayer;
import nl.grapjeje.opengrinding.core.objects.CraftGrindingPlayer;
import nl.grapjeje.opengrinding.jobs.fishing.FishingModule;
import nl.grapjeje.opengrinding.api.player.events.fishing.PlayerFishCatchEvent;
import nl.grapjeje.opengrinding.jobs.fishing.games.ClickGame;
import nl.grapjeje.opengrinding.jobs.fishing.games.FishingGame;
import nl.grapjeje.opengrinding.jobs.fishing.games.MazeGame;
import nl.grapjeje.opengrinding.jobs.fishing.objects.FishLootTable;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerCatchListener implements Listener {
    @Getter
    private static Map<UUID, ItemStack> playerLoot = new HashMap<>();

    private final List<Class<? extends FishingGame>> gameTypes = List.of(
            MazeGame.class,
            ClickGame.class
    );

    private final Random random = new Random();

    @EventHandler
    public void onCatch(PlayerFishEvent e) {
        if (e.getState() == PlayerFishEvent.State.REEL_IN) return;
        FishHook hook = e.getHook();
        if (hook == null) return;

        e.setExpToDrop(0);

        if (hook.getHookedEntity() != null) hook.setHookedEntity(null);
        GrindingRegion region = GrindingRegion.getRegionAt(hook.getLocation(), Jobs.FISHING);
        String value = region != null ? region.getValue() : null;

        Player player = e.getPlayer();

        if (region == null || !region.getJobs().contains(Jobs.FISHING) || value == null || value.isEmpty() ||
                player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {

            String reason;
            if (value == null || value.isEmpty() && region.getJobs().contains(Jobs.FISHING))
                reason = "Er is geen geldige waarde ingesteld voor deze regio!";
            else reason = "Je kan niet vissen in deze gamemode!";
            player.sendMessage(MessageUtil.filterMessage("<red>⚠ " + reason));

            hook.setWaitTime(Integer.MAX_VALUE);
            if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH && e.getCaught() != null) {
                e.setCancelled(true);
                if (hook.isValid()) hook.remove();
            }
            return;
        }

        if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH && e.getCaught() != null) {
            FishLootTable.getRandomItem(value).thenAccept(result -> {
                if (result.hasError()) {
                    player.sendMessage(MessageUtil.filterMessage(result.errorMessage()));
                    if (e.getCaught() instanceof Item itemEntity)
                        itemEntity.setItemStack(ItemStack.of(Material.AIR));
                } else if (result.item() != null) {
                    if (FishingModule.getConfig().isGamesEnabled()) {
                        playerLoot.put(player.getUniqueId(), result.item());
                    } else if (e.getCaught() instanceof Item itemEntity) {
                        itemEntity.setItemStack(result.item());
                        GrindingPlayer.loadOrCreatePlayerModelAsync(player, Jobs.FISHING)
                                .thenAccept(model ->
                                        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                                            GrindingPlayer gp = CraftGrindingPlayer.get(player.getUniqueId(), model);
                                            new PlayerFishCatchEvent(gp, result.item()).callEvent();
                                        }));
                    }
                }
            });
        }

        if (FishingModule.getConfig().isGamesEnabled() && e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            e.setCancelled(true);
            if (hook.isValid()) hook.remove();
            FishingGame game = createRandomGame();
            if (game != null) game.start(player);
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
