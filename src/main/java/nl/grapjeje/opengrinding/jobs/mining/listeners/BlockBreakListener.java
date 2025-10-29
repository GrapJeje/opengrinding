package nl.grapjeje.opengrinding.jobs.mining.listeners;

import net.kyori.adventure.title.TitlePart;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingRegion;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration;
import nl.grapjeje.opengrinding.jobs.mining.objects.MiningOres;
import nl.grapjeje.opengrinding.jobs.mining.objects.Ore;
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

    private static final Set<Material> WHITELIST = Set.of(
            Material.COAL_ORE,
            Material.COPPER_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.REDSTONE_ORE,
            Material.LAPIS_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE
    );

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 500;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Material type = block.getType();
        UUID uuid = player.getUniqueId();

        if (!WHITELIST.contains(type)) return;

        if (!(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            long now = System.currentTimeMillis();
            if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < COOLDOWN_MS) {
                e.setCancelled(true);
                return;
            }
            cooldowns.put(uuid, now);

            e.setCancelled(true);
            e.setDropItems(false);
            e.setExpToDrop(0);
        }

        Material heldItem = player.getInventory().getItemInMainHand().getType();
        if (!heldItem.name().endsWith("PICKAXE")) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je kunt geen ores hakken in deze gamemode!"));
            e.setCancelled(true);
            return;
        }
        MiningModule miningModule = OpenGrinding.getFramework().getModuleLoader()
                .getModules().stream()
                .filter(m -> m instanceof MiningModule)
                .map(m -> (MiningModule) m)
                .findFirst()
                .orElse(null);
        if (miningModule == null || miningModule.isDisabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ De mining module is momenteel uitgeschakeld!"));
            return;
        }
        final Location loc = block.getLocation();
        GrindingRegion.isInRegionWithJob(loc, Jobs.MINING, inRegion -> {
            if (!inRegion) return;

            if (this.isInventoryFull(player)) {
                player.sendTitlePart(TitlePart.TITLE, MessageUtil.filterMessage("<red>Je inventory zit vol!"));
                player.sendTitlePart(TitlePart.SUBTITLE, MessageUtil.filterMessage("<gold>Verkoop wat blokjes!"));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 0.5F);
                return;
            }
            GrindingPlayer.loadOrCreatePlayerModelAsync(player, Jobs.MINING)
                    .thenAccept(model -> {
                        MiningJobConfiguration config = MiningModule.getConfig();

                        String oreKey = type.name().replace("_ORE", "");
                        Ore oreEnum = Ore.valueOf(oreKey);
                        MiningJobConfiguration.OreRecord oreRecord = config.getOre(oreEnum);
                        if (oreRecord == null) return;

                        int unlockLevel = oreRecord.unlockLevel();

                        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                            if (model.getLevel() < unlockLevel) {
                                block.setType(type);
                                player.sendMessage(MessageUtil.filterMessage(
                                        "<warning>⚠ Jij bent niet hoog genoeg level voor dit blok! (Nodig: " + unlockLevel + ")"
                                ));
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5F, 1.0F);
                                return;
                            }
                            ItemStack custom = MiningModule.getBlockHead(type);
                            if (custom != null) player.getInventory().addItem(custom);

                            block.setType(Material.BEDROCK);
                            MiningModule.getOres().add(new MiningOres(loc, type, System.currentTimeMillis()));

                            ItemStack item = player.getInventory().getItemInMainHand();
                            if (item != null && item.getType() != Material.AIR) {
                                ItemMeta meta = item.getItemMeta();
                                if (meta instanceof Damageable damageable) {
                                    int currentDamage = damageable.getDamage();
                                    int maxDurability = item.getType().getMaxDurability();

                                    if (currentDamage + 1 < maxDurability)
                                        damageable.setDamage(currentDamage + 1);
                                    else
                                        item.setAmount(item.getAmount() - 1);

                                    item.setItemMeta(damageable);
                                }
                            }
                            player.giveExp(0);

                            CompletableFuture.runAsync(() -> {
                                GrindingPlayer gp = new GrindingPlayer(player.getUniqueId(), model);
                                gp.addProgress(Jobs.MINING, oreRecord.points());
                                gp.save(Jobs.MINING);
                            });
                        });
                    });
        });
    }

    private boolean isInventoryFull(Player player) {
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) return false;
            if (item.getAmount() < item.getMaxStackSize()) return false;
        }
        return true;
    }
}
