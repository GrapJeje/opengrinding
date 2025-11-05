package nl.grapjeje.opengrinding.jobs.farming.listeners;

import net.kyori.adventure.title.TitlePart;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.api.GrindingRegion;
import nl.grapjeje.opengrinding.api.Jobs;
import nl.grapjeje.opengrinding.api.ToolType;
import nl.grapjeje.opengrinding.api.player.GrindingPlayer;
import nl.grapjeje.opengrinding.jobs.core.objects.CraftGrindingPlayer;
import nl.grapjeje.opengrinding.jobs.farming.FarmingModule;
import nl.grapjeje.opengrinding.jobs.farming.configuration.FarmingJobConfiguration;
import nl.grapjeje.opengrinding.jobs.farming.objects.GrowthStage;
import nl.grapjeje.opengrinding.jobs.farming.objects.Plant;
import nl.grapjeje.opengrinding.jobs.farming.plants.WheatPlant;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class FarmingListener implements Listener {
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 500;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWheatFarm(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (block.getType() != Material.WHEAT) return;
        Player player = e.getPlayer();
        ToolType toolType = ToolType.fromItem(player.getInventory().getItemInMainHand());

        this.canHarvest(player, block, Plant.WHEAT).thenAccept(result -> {
            boolean canHarvest = result.keySet().iterator().next();
            boolean shouldCancel = result.values().iterator().next();

            if (shouldCancel) {
                e.setCancelled(true);
                e.setDropItems(false);
                e.setExpToDrop(0);
            }
            if (canHarvest) {
                UUID blockId = UUID.randomUUID();
                Location blockLocation = block.getLocation();

                Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
                    AtomicReference<WheatPlant> existing = new AtomicReference<>(null);

                    FarmingModule.getPlants().forEach(plant -> {
                        if (!(plant instanceof WheatPlant)) return;
                        if (plant.getBlock().getLocation().equals(blockLocation))
                            existing.set((WheatPlant) plant);
                    });
                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                        if (existing.get() != null)
                            existing.get().onInteract(player, toolType, block);
                        else {
                            WheatPlant newPlant = null;
                            if (block.getBlockData() instanceof Ageable ageable) {
                                if (ageable.getAge() >= ageable.getMaximumAge())
                                    newPlant = new WheatPlant(blockId, block, GrowthStage.READY);
                            } else newPlant = new WheatPlant(blockId, block);
                            FarmingModule.getPlants().add(newPlant);
                        }
                    });
                });
            }
        });
    }

    // First boolean is if we can harvest it, the second one is if we need to cancel the event
    private CompletableFuture<Map<Boolean, Boolean>> canHarvest(Player player, Block block, Plant plant) {
        CompletableFuture<Map<Boolean, Boolean>> result = new CompletableFuture<>();

        FarmingModule farmingModule = OpenGrinding.getFramework().getModuleLoader()
                .getModules().stream()
                .filter(m -> m instanceof FarmingModule)
                .map(m -> (FarmingModule) m)
                .findFirst()
                .orElse(null);

        if (farmingModule == null || farmingModule.isDisabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ De farming module is momenteel uitgeschakeld!"));
            result.complete(Map.of(false, true));
            return result;
        }

        if (!(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            long now = System.currentTimeMillis();
            UUID uuid = player.getUniqueId();
            if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < COOLDOWN_MS) {
                result.complete(Map.of(false, true));
                return result;
            }
            cooldowns.put(uuid, now);
        }

        Location location = block.getLocation();
        GrindingRegion.isInRegionWithJob(location, Jobs.FARMING, inRegion -> {
            if (!inRegion) {
                block.breakNaturally(player.getInventory().getItemInMainHand());
                result.complete(Map.of(false, false));
                return;
            }

            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                    player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je kunt niet farmen in deze gamemode!"));
                    result.complete(Map.of(false, false));
                    return;
                }

                GrindingPlayer.loadOrCreatePlayerModelAsync(player, Jobs.FARMING)
                        .thenAccept(model -> {
                            if (CraftGrindingPlayer.get(player.getUniqueId(), model).isInventoryFull()) {
                                player.sendTitlePart(TitlePart.TITLE, MessageUtil.filterMessage("<red>Je inventory zit vol!"));
                                player.sendTitlePart(TitlePart.SUBTITLE, MessageUtil.filterMessage("<gold>Verkoop wat blokjes!"));
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 0.5F);
                                result.complete(Map.of(false, true));
                                return;
                            }

                            FarmingJobConfiguration config = FarmingModule.getConfig();
                            FarmingJobConfiguration.PlantRecord plantRecord = config.getPlants().get(plant);
                            if (plantRecord == null) {
                                result.complete(Map.of(true, false));
                                return;
                            }

                            int unlockLevel = plantRecord.unlockLevel();
                            if (model.getLevel() < unlockLevel) {
                                player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Jij bent niet hoog genoeg level voor dit blok! (Nodig: " + unlockLevel + ")"));
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5F, 1.0F);
                                result.complete(Map.of(false, true));
                                return;
                            }

                            result.complete(Map.of(true, false));
                        });
            });
        });
        return result;
    }
}
