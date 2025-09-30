package nl.grapjeje.opengrinding.jobs.mining.listener;

import com.craftmend.storm.api.enums.Where;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingRegion;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlockBreakListener implements Listener {

    private static final List<Material> whitelist = new ArrayList<>();
    static {
        whitelist.add(Material.COAL_ORE);
        whitelist.add(Material.COPPER_ORE);
        whitelist.add(Material.IRON_ORE);
        whitelist.add(Material.GOLD_ORE);
        whitelist.add(Material.REDSTONE_ORE);
        whitelist.add(Material.LAPIS_ORE);
        whitelist.add(Material.DIAMOND_ORE);
        whitelist.add(Material.EMERALD_ORE);
        // TODO: Add raw ore blocks and deepslate
    }

    // TODO: Checken op level
    // TODO: Checken op whitelist

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (!GrindingRegion.isInRegionWithJob(e.getBlock().getLocation(), Jobs.MINING)) return;

        Material heldItem = player.getInventory().getItemInMainHand().getType();
        if (!heldItem.name().endsWith("PICKAXE")) return;

        e.setDropItems(false);
        e.setExpToDrop(0);

        if (this.isInventoryFull(player)) {
            Component title = MessageUtil.filterMessage("<red>Je inventory zit vol!");
            Component subtitle = MessageUtil.filterMessage("<gold>Verkoop wat blokjes!");
            player.sendTitlePart(TitlePart.TITLE, title);
            player.sendTitlePart(TitlePart.SUBTITLE, subtitle);
            player.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(1000)));

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 0.5F);
            e.setCancelled(true);
            return;
        }

        ItemStack item = MiningModule.getBlockHead(e.getBlock());
        player.getInventory().addItem(item);

        Optional<PlayerGrindingModel> playerModel;
        try {
            playerModel = StormDatabase.getInstance().getStorm()
                    .buildQuery(PlayerGrindingModel.class)
                    .where("player_uuid", Where.EQUAL, player.getUniqueId())
                    .where("job_name", Where.EQUAL, Jobs.MINING.name())
                    .limit(1)
                    .execute()
                    .join()
                    .stream()
                    .findFirst();
        } catch (Exception ex) {
            ex.printStackTrace();
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het ophalen van jouw spelersdata!"));
            return;
        }

        Material blockType = e.getBlock().getType();
        String baseName = blockType.name()
                .replace("_ORE", "")
                .toLowerCase();

        int points = CoreModule.getGrindingLevelsConfiguration().getPointsPerOre().getOrDefault(baseName, 0);

        PlayerGrindingModel model;
        if (playerModel.isEmpty()) {
            model = new PlayerGrindingModel();
            model.setPlayerUuid(player.getUniqueId());
            model.setJob(Jobs.MINING);
            model.setLevel(0);
            model.setValue(0.0);
            Bukkit.getLogger().info("New player grind model made for " + player.getName());
        } else model = playerModel.get();

        GrindingPlayer gp = new GrindingPlayer(player.getUniqueId(), model);
        gp.addProgress(Jobs.MINING, points);
        gp.save();
    }

    private boolean isInventoryFull(Player player) {
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR)
                return false;
            if (item.getAmount() < item.getMaxStackSize())
                return false;
        }
        return true;
    }
}
