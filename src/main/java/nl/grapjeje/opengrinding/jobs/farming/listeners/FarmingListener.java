package nl.grapjeje.opengrinding.jobs.farming.listeners;

import net.kyori.adventure.title.TitlePart;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.api.GrindingRegion;
import nl.grapjeje.opengrinding.api.Jobs;
import nl.grapjeje.opengrinding.api.ToolType;
import nl.grapjeje.opengrinding.api.player.GrindingPlayer;
import nl.grapjeje.opengrinding.core.objects.CraftGrindingPlayer;
import nl.grapjeje.opengrinding.jobs.farming.FarmingModule;
import nl.grapjeje.opengrinding.jobs.farming.configuration.FarmingJobConfiguration;
import nl.grapjeje.opengrinding.jobs.farming.objects.GrowthStage;
import nl.grapjeje.opengrinding.jobs.farming.objects.Plant;
import nl.grapjeje.opengrinding.jobs.farming.plants.BeetRootPlant;
import nl.grapjeje.opengrinding.jobs.farming.plants.CarrotPlant;
import nl.grapjeje.opengrinding.jobs.farming.plants.PotatoPlant;
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
    private static final long COOLDOWN_MS = 50;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBeetRootFarm(BlockBreakEvent e) {
        final Block block = e.getBlock();
        Player player = e.getPlayer();
        if (block.getType() != Material.BEETROOTS) return;

        ToolType toolType = ToolType.fromItem(player.getInventory().getItemInMainHand());
        e.setCancelled(true);
        e.setDropItems(false);
        e.setExpToDrop(0);

        if (!this.canHarvestSync(player)) return;
        this.canHarvestAsync(player, block, Plant.BEETROOT, toolType)
                .thenAccept(canHarvest -> {
                    if (canHarvest) {
                        UUID blockId = UUID.randomUUID();
                        Location blockLocation = block.getLocation();

                        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
                            AtomicReference<BeetRootPlant> existing = new AtomicReference<>(null);

                            FarmingModule.getPlants().forEach(plant -> {
                                if (!(plant instanceof BeetRootPlant)) return;
                                if (plant.getBlock().getLocation().equals(blockLocation))
                                    existing.set((BeetRootPlant) plant);
                            });

                            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                                if (existing.get() != null)
                                    existing.get().onInteract(player, toolType, block);
                                else {
                                    BeetRootPlant newPlant = null;
                                    if (block.getBlockData() instanceof Ageable ageable) {
                                        if (ageable.getAge() >= ageable.getMaximumAge())
                                            newPlant = new BeetRootPlant(blockId, block, GrowthStage.READY);
                                    } else newPlant = new BeetRootPlant(blockId, block);
                                    if (newPlant == null) return;

                                    FarmingModule.getPlants().add(newPlant);
                                    newPlant.onInteract(player, toolType, block);
                                }
                            });
                        });
                    }
                });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWheatFarm(BlockBreakEvent e) {
        final Block block = e.getBlock();
        Player player = e.getPlayer();

        if (block.getType() != Material.WHEAT) return;
        ToolType toolType = ToolType.fromItem(player.getInventory().getItemInMainHand());
        e.setCancelled(true);
        e.setDropItems(false);
        e.setExpToDrop(0);

        if (!this.canHarvestSync(player)) return;
        this.canHarvestAsync(player, block, Plant.WHEAT, toolType)
                .thenAccept(canHarvest -> {
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
                                    } else
                                        newPlant = new WheatPlant(blockId, block);
                                    if (newPlant == null) return;
                                    FarmingModule.getPlants().add(newPlant);
                                    newPlant.onInteract(player, toolType, block);
                                }
                            });
                        });
                    }
                });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCarrotFarm(BlockBreakEvent e) {
        final Block block = e.getBlock();
        Player player = e.getPlayer();

        if (block.getType() != Material.CARROT) return;
        ToolType toolType = ToolType.fromItem(player.getInventory().getItemInMainHand());
        e.setCancelled(true);
        e.setDropItems(false);
        e.setExpToDrop(0);

        if (!this.canHarvestSync(player)) return;
        this.canHarvestAsync(player, block, Plant.CARROT, toolType)
                .thenAccept(canHarvest -> {
                    if (canHarvest) {
                        UUID blockId = UUID.randomUUID();
                        Location blockLocation = block.getLocation();

                        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
                            AtomicReference<CarrotPlant> existing = new AtomicReference<>(null);

                            FarmingModule.getPlants().forEach(plant -> {
                                if (!(plant instanceof CarrotPlant)) return;
                                if (plant.getBlock().getLocation().equals(blockLocation))
                                    existing.set((CarrotPlant) plant);
                            });

                            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                                if (existing.get() != null)
                                    existing.get().onInteract(player, toolType, block);
                                else {
                                    CarrotPlant newPlant = null;
                                    if (block.getBlockData() instanceof Ageable ageable) {
                                        if (ageable.getAge() >= ageable.getMaximumAge())
                                            newPlant = new CarrotPlant(blockId, block, GrowthStage.READY);
                                    } else
                                        newPlant = new CarrotPlant(blockId, block);
                                    if (newPlant == null) return;
                                    FarmingModule.getPlants().add(newPlant);
                                    newPlant.onInteract(player, toolType, block);
                                }
                            });
                        });
                    }
                });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotatoFarm(BlockBreakEvent e) {
        final Block block = e.getBlock();
        Player player = e.getPlayer();

        if (block.getType() != Material.POTATO) return;
        ToolType toolType = ToolType.fromItem(player.getInventory().getItemInMainHand());
        e.setCancelled(true);
        e.setDropItems(false);
        e.setExpToDrop(0);

        if (!this.canHarvestSync(player)) return;
        this.canHarvestAsync(player, block, Plant.POTATO, toolType)
                .thenAccept(canHarvest -> {
                    if (canHarvest) {
                        UUID blockId = UUID.randomUUID();
                        Location blockLocation = block.getLocation();

                        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
                            AtomicReference<PotatoPlant> existing = new AtomicReference<>(null);

                            FarmingModule.getPlants().forEach(plant -> {
                                if (!(plant instanceof PotatoPlant)) return;
                                if (plant.getBlock().getLocation().equals(blockLocation))
                                    existing.set((PotatoPlant) plant);
                            });

                            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                                if (existing.get() != null)
                                    existing.get().onInteract(player, toolType, block);
                                else {
                                    PotatoPlant newPlant = null;
                                    if (block.getBlockData() instanceof Ageable ageable) {
                                        if (ageable.getAge() >= ageable.getMaximumAge())
                                            newPlant = new PotatoPlant(blockId, block, GrowthStage.READY);
                                    } else
                                        newPlant = new PotatoPlant(blockId, block);
                                    if (newPlant == null) return;
                                    FarmingModule.getPlants().add(newPlant);
                                    newPlant.onInteract(player, toolType, block);
                                }
                            });
                        });
                    }
                });
    }

    private boolean canHarvestSync(Player player) {
        FarmingModule farmingModule = OpenGrinding.getFramework().getModuleLoader()
                .getModules().stream()
                .filter(m -> m instanceof FarmingModule)
                .map(m -> (FarmingModule) m)
                .findFirst()
                .orElse(null);

        if (farmingModule == null || farmingModule.isDisabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ De farming module is momenteel uitgeschakeld!"));
            return false;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < COOLDOWN_MS)
            return false;
        cooldowns.put(uuid, now);

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je kunt niet farmen in deze gamemode!"));
            return false;
        }
        return true;
    }

    private CompletableFuture<Boolean> canHarvestAsync(Player player, Block block, Plant plant, ToolType toolType) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        GrindingRegion.isInRegionWithJobAsync(block.getLocation(), Jobs.FARMING, inRegion -> {
            if (!inRegion) {
                result.complete(false);
                return;
            }
            GrindingPlayer.loadOrCreatePlayerModelAsync(player, Jobs.FARMING)
                    .thenAccept(model -> {
                        GrindingPlayer craftPlayer = CraftGrindingPlayer.get(player.getUniqueId(), model);

                        if (craftPlayer.isInventoryFull()) {
                            player.sendTitlePart(TitlePart.TITLE, MessageUtil.filterMessage("<red>Je inventory zit vol!"));
                            player.sendTitlePart(TitlePart.SUBTITLE, MessageUtil.filterMessage("<gold>Verkoop wat blokjes!"));
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 0.5F);
                            result.complete(false);
                            return;
                        }
                        FarmingJobConfiguration.PlantRecord record = FarmingModule.getConfig().getPlants().get(plant);
                        if (record == null || model.getLevel() < record.unlockLevel()) {
                            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je bent niet hoog genoeg level voor dit blok!"));
                            result.complete(false);
                            return;
                        }
                        result.complete(true);
                    }).exceptionally(ex -> {
                        ex.printStackTrace();
                        result.complete(false);
                        return null;
                    });
        });
        return result;
    }
}
