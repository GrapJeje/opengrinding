package nl.grapjeje.opengrinding.jobs.mining.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingRegion;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.jobs.mining.objects.MiningOres;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration;
import nl.grapjeje.opengrinding.jobs.mining.objects.Ore;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.*;

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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < COOLDOWN_MS) {
            e.setCancelled(true);
            return;
        }
        cooldowns.put(uuid, now);

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
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        Block block = e.getBlock();
        final Material originalType = block.getType();
        final Location location = block.getLocation();
        GrindingRegion.isInRegionWithJob(location, Jobs.MINING, inRegion -> {
            if (!inRegion) return;

            Material heldItem = player.getInventory().getItemInMainHand().getType();
            if (!heldItem.name().endsWith("PICKAXE")) return;

            if (!this.canMine(block)) return;
            e.setDropItems(false);
            e.setExpToDrop(0);

            Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
                PlayerGrindingModel model = GrindingPlayer.loadOrCreatePlayerModel(player, Jobs.MINING);
                MiningJobConfiguration config = MiningModule.getConfig();

                String oreKey = originalType.name().replace("_ORE", "").toUpperCase();
                Ore oreEnum;
                try {
                    oreEnum = Ore.valueOf(oreKey);
                } catch (IllegalArgumentException ex) {
                    return;
                }

                MiningJobConfiguration.OreRecord oreRecord = config.getOre(oreEnum);
                if (oreRecord == null) return;

                if (model.getLevel() < oreRecord.unlockLevel()) {
                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                        e.setCancelled(true);
                        block.setType(originalType);
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Jij bent niet hoog genoeg level voor dit blok! (Nodig: " + oreRecord.unlockLevel() + ")"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5F, 1.0F);
                    });
                    return;
                }
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                    if (this.isInventoryFull(player)) {
                        e.setCancelled(true);
                        block.setType(originalType);
                        Component title = MessageUtil.filterMessage("<red>Je inventory zit vol!");
                        Component subtitle = MessageUtil.filterMessage("<gold>Verkoop wat blokjes!");
                        player.sendTitlePart(TitlePart.TITLE, title);
                        player.sendTitlePart(TitlePart.SUBTITLE, subtitle);
                        player.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(1000)));
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 0.5F);
                        return;
                    }

                    ItemStack item = MiningModule.getBlockHead(originalType);
                    if (item != null) player.getInventory().addItem(item);
                    else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het geven van de ore"));

                    block.setType(Material.BEDROCK);
                    MiningModule.getOres().add(new MiningOres(location, originalType, System.currentTimeMillis()));

                    GrindingPlayer gp = new GrindingPlayer(player.getUniqueId(), model);
                    gp.addProgress(Jobs.MINING, oreRecord.points());

                    Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(),
                            () -> gp.save(Jobs.MINING));
                });
            });
        });
    }

    private boolean canMine(Block block) {
        return WHITELIST.contains(block.getType());
    }

    private boolean isInventoryFull(Player player) {
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) return false;
            if (item.getAmount() < item.getMaxStackSize()) return false;
        }
        return true;
    }
}
