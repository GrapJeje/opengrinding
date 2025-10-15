package nl.grapjeje.opengrinding.jobs.lumber.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingRegion;
import nl.grapjeje.opengrinding.jobs.lumber.LumberModule;
import nl.grapjeje.opengrinding.jobs.lumber.configuration.LumberJobConfiguration;
import nl.grapjeje.opengrinding.jobs.lumber.objects.Wood;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class BlockBreakListener implements Listener {
    private static final Set<Material> WHITELIST = createWhitelist();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
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
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        Block block = e.getBlock();
        final Material originalType = block.getType();
        final Location location = block.getLocation();

        if (!GrindingRegion.isInRegionWithJob(location, Jobs.LUMBER)) return;
        if (!this.canMine(block)) return;

        Material heldItem = player.getInventory().getItemInMainHand().getType();
        if (!heldItem.name().endsWith("AXE") || heldItem.name().endsWith("PICKAXE")) return;

        e.setDropItems(false);

        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
            PlayerGrindingModel model = GrindingPlayer.loadOrCreatePlayerModel(player, Jobs.LUMBER);
            LumberJobConfiguration config = LumberModule.getConfig();

            Wood woodEnum = null;
            String woodType = null;
            for (Wood wood : Wood.values()) {
                if (originalType == wood.getBarkMaterial()) {
                    woodEnum = wood;
                    woodType = "bark";
                    break;
                }
                if (originalType == wood.getStrippedMaterial()) {
                    woodEnum = wood;
                    woodType = "wood";
                    break;
                }
            }
            if (woodEnum == null || woodType == null) return;

            LumberJobConfiguration.WoodRecord woodRecord = config.getWood(woodEnum);
            if (woodRecord == null) return;

            int unlockLevel = woodRecord.unlockLevels().getOrDefault(woodType, 0);

            if (model.getLevel() < unlockLevel) {
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                    e.setCancelled(true);
                    block.setType(originalType);
                    player.sendMessage(MessageUtil.filterMessage(
                            "<warning>⚠ Jij bent niet hoog genoeg level voor dit blok! (Nodig: " + unlockLevel + ")"
                    ));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5F, 1.0F);
                });
                return;
            }

            Wood finalWoodEnum = woodEnum;
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                if (this.isInventoryFull(player)) {
                    e.setCancelled(true);
                    block.setType(originalType);
                    Component title = MessageUtil.filterMessage("<red>Je inventory zit vol!");
                    Component subtitle = MessageUtil.filterMessage("<gold>Verkoop wat blokjes!");
                    player.sendTitlePart(TitlePart.TITLE, title);
                    player.sendTitlePart(TitlePart.SUBTITLE, subtitle);
                    player.sendTitlePart(TitlePart.TIMES, Title.Times.times(
                            Duration.ofMillis(500),
                            Duration.ofMillis(3500),
                            Duration.ofMillis(1000)
                    ));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 0.5F);
                    return;
                }

                ItemStack item = LumberModule.getWoodHead(originalType);
                if (item != null) player.getInventory().addItem(item);

                if (originalType == finalWoodEnum.getBarkMaterial()) {
                    block.setType(finalWoodEnum.getStrippedMaterial());
                    LumberModule.getWoods().add(new LumberModule.LumberWood(location, originalType, System.currentTimeMillis()));
                } else if (originalType == finalWoodEnum.getStrippedMaterial()) {
                    block.setType(Material.AIR);
                    for (LumberModule.LumberWood wood : LumberModule.getWoods()) {
                        if (wood.location() != location) continue;
                        Material type = wood.material();
                        LumberModule.getWoods().remove(wood);
                        LumberModule.getWoods().add(new LumberModule.LumberWood(location, type, System.currentTimeMillis()));
                    }
                }

                Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
                    GrindingPlayer gp = new GrindingPlayer(player.getUniqueId(), model);
                    gp.addProgress(Jobs.LUMBER, woodRecord.points());
                    gp.save();
                });
            });
        });
    }

    private boolean canMine(Block block) {
        return WHITELIST.contains(block.getType());
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

    private boolean isInventoryFull(Player player) {
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) return false;
            if (item.getAmount() < item.getMaxStackSize()) return false;
        }
        return true;
    }
}
