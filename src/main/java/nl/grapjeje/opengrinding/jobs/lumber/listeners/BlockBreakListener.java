package nl.grapjeje.opengrinding.jobs.lumber.listeners;

import net.kyori.adventure.title.TitlePart;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.api.GrindingRegion;
import nl.grapjeje.opengrinding.api.Jobs;
import nl.grapjeje.opengrinding.api.player.GrindingPlayer;
import nl.grapjeje.opengrinding.jobs.core.objects.CraftGrindingPlayer;
import nl.grapjeje.opengrinding.jobs.lumber.LumberModule;
import nl.grapjeje.opengrinding.jobs.lumber.configuration.LumberJobConfiguration;
import nl.grapjeje.opengrinding.jobs.lumber.objects.Wood;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BlockBreakListener implements Listener {
    private static final Set<Material> WHITELIST = createWhitelist();
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 500;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Material type = block.getType();
        UUID uuid = player.getUniqueId();

        LumberModule lumberModule = OpenGrinding.getFramework().getModuleLoader()
                .getModules().stream()
                .filter(m -> m instanceof LumberModule)
                .map(m -> (LumberModule) m)
                .findFirst()
                .orElse(null);
        if (lumberModule == null || lumberModule.isDisabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ De lumber module is momenteel uitgeschakeld!"));
            return;
        }

        if (!WHITELIST.contains(type)) return;

        if (!(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            e.setCancelled(true);
            e.setDropItems(false);
            e.setExpToDrop(0);
            long now = System.currentTimeMillis();
            if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < COOLDOWN_MS)
                return;
            cooldowns.put(uuid, now);
        }
        final Location location = block.getLocation();
        GrindingRegion.isInRegionWithJob(location, Jobs.LUMBER, inRegion -> {
            if (!inRegion) {
                block.breakNaturally(player.getInventory().getItemInMainHand());
                return;
            }
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                Material heldItem = player.getInventory().getItemInMainHand().getType();
                if (!heldItem.name().endsWith("AXE") && heldItem.name().endsWith("PICKAXE")) return;
                if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                    player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je kunt geen ores hakken in deze gamemode!"));
                    return;
                }
                GrindingPlayer.loadOrCreatePlayerModelAsync(player, Jobs.LUMBER)
                        .thenAccept(model -> {
                            if (CraftGrindingPlayer.get(player.getUniqueId(), model).isInventoryFull()) {
                                player.sendTitlePart(TitlePart.TITLE, MessageUtil.filterMessage("<red>Je inventory zit vol!"));
                                player.sendTitlePart(TitlePart.SUBTITLE, MessageUtil.filterMessage("<gold>Verkoop wat blokjes!"));
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 0.5F);
                                return;
                            }
                            LumberJobConfiguration config = LumberModule.getConfig();

                            Wood woodEnum = null;
                            String woodType = null;
                            for (Wood wood : Wood.values()) {
                                if (type == wood.getBarkMaterial()) {
                                    woodEnum = wood;
                                    woodType = "bark";
                                    break;
                                }
                                if (type == wood.getStrippedMaterial()) {
                                    woodEnum = wood;
                                    woodType = "wood";
                                    break;
                                }
                            }
                            if (woodEnum == null || woodType == null) return;

                            LumberJobConfiguration.WoodRecord woodRecord = config.getWood(woodEnum);
                            if (woodRecord == null) return;

                            int unlockLevel = woodRecord.unlockLevels().getOrDefault(woodType, 0);

                            Wood finalWoodEnum = woodEnum;
                            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                                if (model.getLevel() < unlockLevel) {
                                    e.setCancelled(true);
                                    block.setType(type);
                                    player.sendMessage(MessageUtil.filterMessage(
                                            "<warning>⚠ Jij bent niet hoog genoeg level voor dit blok! (Nodig: " + unlockLevel + ")"
                                    ));
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5F, 1.0F);
                                    return;
                                }

                                ItemStack item = LumberModule.getWoodHead(type);
                                if (item != null) player.getInventory().addItem(item);

                                if (type == finalWoodEnum.getBarkMaterial()) {
                                    block.setType(finalWoodEnum.getStrippedMaterial());
                                    LumberModule.getWoods().add(new LumberModule.LumberWood(location, type, System.currentTimeMillis()));
                                } else if (type == finalWoodEnum.getStrippedMaterial()) {
                                    block.setType(Material.AIR);
                                    LumberModule.getWoods().removeIf(wood -> wood.location().equals(location));
                                    LumberModule.getWoods().add(new LumberModule.LumberWood(location, finalWoodEnum.getBarkMaterial(), System.currentTimeMillis()));
                                }

                                ItemStack handItem = player.getInventory().getItemInMainHand();
                                if (handItem != null && handItem.getType() != Material.AIR) {
                                    ItemMeta meta = handItem.getItemMeta();
                                    if (meta instanceof Damageable damageable) {
                                        int currentDamage = damageable.getDamage();
                                        int maxDurability = handItem.getType().getMaxDurability();

                                        if (currentDamage + 1 < maxDurability)
                                            damageable.setDamage(currentDamage + 1);
                                        else handItem.setAmount(handItem.getAmount() - 1);
                                        handItem.setItemMeta(damageable);
                                    }
                                }

                                CompletableFuture.runAsync(() -> {
                                    GrindingPlayer gp = CraftGrindingPlayer.get(player.getUniqueId(), model);
                                    gp.addProgress(Jobs.LUMBER, woodRecord.points());
                                    gp.save(Jobs.LUMBER);
                                });
                            });
                        });
            });
        });
    }

    private static Set<Material> createWhitelist() {
        Set<Material> set = new HashSet<>();
        set.add(Material.OAK_WOOD);
        set.add(Material.STRIPPED_OAK_WOOD);
        set.add(Material.SPRUCE_WOOD);
        set.add(Material.STRIPPED_SPRUCE_WOOD);
        set.add(Material.BIRCH_WOOD);
        set.add(Material.STRIPPED_BIRCH_WOOD);
        set.add(Material.JUNGLE_WOOD);
        set.add(Material.STRIPPED_JUNGLE_WOOD);
        set.add(Material.ACACIA_WOOD);
        set.add(Material.STRIPPED_ACACIA_WOOD);
        set.add(Material.DARK_OAK_WOOD);
        set.add(Material.STRIPPED_DARK_OAK_WOOD);
        set.add(Material.MANGROVE_WOOD);
        set.add(Material.STRIPPED_MANGROVE_WOOD);
        set.add(Material.CHERRY_WOOD);
        set.add(Material.STRIPPED_CHERRY_WOOD);
        set.add(Material.CRIMSON_HYPHAE);
        set.add(Material.STRIPPED_CRIMSON_HYPHAE);
        set.add(Material.WARPED_HYPHAE);
        set.add(Material.STRIPPED_WARPED_HYPHAE);

        try {
            set.add(Material.valueOf("PALE_OAK_WOOD"));
            set.add(Material.valueOf("STRIPPED_PALE_OAK_WOOD"));
        } catch (IllegalArgumentException ignored) {
        }

        return Set.copyOf(set);
    }
}
