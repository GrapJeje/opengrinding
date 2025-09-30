package nl.grapjeje.opengrinding.jobs.core.listeners;

import net.kyori.adventure.text.Component;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.jobs.core.commands.GrindingRegionCommand;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingRegion;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerRegionWandListener implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!GrindingRegionCommand.getSelections().containsKey(player.getUniqueId())) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BLAZE_ROD || !item.hasItemMeta()) return;

        Component name = MessageUtil.filterMessage("<green>Grinding Region Stick");
        if (!name.equals(item.getItemMeta().itemName())) return;

        Block block = e.getClickedBlock();
        if (block == null) return;
        Location location = block.getLocation();

        GrindingRegionCommand.RegionSelection selection = GrindingRegionCommand.getSelections().get(player.getUniqueId());
        Action action = e.getAction();
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK)
            e.setCancelled(true);

        if (action == Action.LEFT_CLICK_BLOCK)
            selection.setMin(location);
        else if (action == Action.RIGHT_CLICK_BLOCK)
            selection.setMax(location);

        this.sendRegionPointMessage(player, action, location);

        if (selection.getMin() != null && selection.getMax() != null && !selection.isFinished()) {
            selection.setFinished(true);
            GrindingRegion region = new GrindingRegion(new GrindingRegionModel());
            region.setName(selection.getName());
            region.setMinLocation(selection.getMin());
            region.setMaxLocation(selection.getMax());
            region.save();

            GrindingRegionCommand.getSelections().remove(player.getUniqueId());
            player.getInventory().remove(item);
            player.sendMessage(MessageUtil.filterMessage("<green><bold>" + selection.getName() + " </bold>grinding region succesvol aangemaakt!"));
        }
    }

    private void sendRegionPointMessage(Player player, Action action, Location location) {
        String pointNumber = action == Action.LEFT_CLICK_BLOCK ? "#1" : "#2";
        String message = String.format(
                "<yellow>%s x:%d y:%d z:%d",
                pointNumber,
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
        player.sendMessage(MessageUtil.filterMessage(message));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (player.getActiveItemHand() != EquipmentSlot.HAND) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BLAZE_ROD || !item.hasItemMeta()) return;

        Component name = MessageUtil.filterMessage("<green>Grinding Region Stick");
        if (name.equals(item.getItemMeta().itemName()))
            e.setCancelled(true);
    }
}
