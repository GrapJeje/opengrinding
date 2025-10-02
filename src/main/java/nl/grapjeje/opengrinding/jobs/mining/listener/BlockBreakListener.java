package nl.grapjeje.opengrinding.jobs.mining.listener;

import com.craftmend.storm.api.enums.Where;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingRegion;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.jobs.mining.objects.MiningOres;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration.OreRecord;
import nl.grapjeje.opengrinding.jobs.mining.objects.Ore;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
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

        Block block = e.getBlock();
        Location location = block.getLocation();
        Material originalType = block.getType();
        if (!GrindingRegion.isInRegionWithJob(location, Jobs.MINING)) return;

        Material heldItem = player.getInventory().getItemInMainHand().getType();
        if (!heldItem.name().endsWith("PICKAXE")) return;

        if (!this.canMine(block)) return;

        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
            PlayerGrindingModel model = this.loadOrCreatePlayerModel(player);
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

                e.setDropItems(false);
                e.setExpToDrop(0);

                ItemStack item = MiningModule.getBlockHead(originalType);
                if (item != null) player.getInventory().addItem(item);
                else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het geven van de ore"));

                block.setType(Material.BEDROCK);
                MiningModule.getOres().add(new MiningOres(location, originalType, System.currentTimeMillis()));

                GrindingPlayer gp = new GrindingPlayer(player.getUniqueId(), model);
                gp.addProgress(Jobs.MINING, oreRecord.points());

                Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), gp::save);
            });
        });
    }

    private PlayerGrindingModel loadOrCreatePlayerModel(Player player) {
        if (CoreModule.getPlayerCache().containsKey(player.getUniqueId()))
            return CoreModule.getPlayerCache().get(player.getUniqueId());

        Optional<PlayerGrindingModel> playerModelOpt;
        try {
            playerModelOpt = StormDatabase.getInstance().getStorm()
                    .buildQuery(PlayerGrindingModel.class)
                    .where("player_uuid", Where.EQUAL , player.getUniqueId())
                    .where("job_name", Where.EQUAL, Jobs.MINING.name())
                    .limit(1)
                    .execute()
                    .join()
                    .stream()
                    .findFirst();
        } catch (Exception ex) {
            ex.printStackTrace();
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                    player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het ophalen van jouw spelersdata!"))
            );
            throw new RuntimeException(ex);
        }

        return playerModelOpt.orElseGet(() -> {
            PlayerGrindingModel m = new PlayerGrindingModel();
            m.setPlayerUuid(player.getUniqueId());
            m.setJob(Jobs.MINING);
            m.setLevel(0);
            m.setValue(0.0);
            Bukkit.getLogger().info("New player grind model made for " + player.getName());
            return m;
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
