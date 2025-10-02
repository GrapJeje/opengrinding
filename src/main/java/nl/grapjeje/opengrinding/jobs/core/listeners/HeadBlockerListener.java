package nl.grapjeje.opengrinding.jobs.core.listeners;

import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.mining.objects.Ore;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.event.inventory.ClickType;

public class HeadBlockerListener implements Listener {

    @EventHandler
    public void onEquip(InventoryClickEvent e) {
        if (CoreModule.getConfig().isJobSkullsOnPlayerhead()) return;
        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack itemToCheck;
        boolean shiftClick = false;

        if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
            itemToCheck = e.getCurrentItem();
            shiftClick = true;
        } else {
            if (e.getSlot() != 39) return;
            itemToCheck = e.getCursor();
        }

        if (itemToCheck == null) return;
        if (itemToCheck.getType() != Material.PLAYER_HEAD) return;
        if (!itemToCheck.hasItemMeta()) return;

        SkullMeta meta = (SkullMeta) itemToCheck.getItemMeta();
        if (meta == null || meta.getPlayerProfile() == null) return;

        for (Ore ore : Ore.values()) {
            if (meta.getPlayerProfile().getId().equals(ore.getUuid())) {
                e.setCancelled(true);
                if (shiftClick) player.getInventory().setItem(e.getSlot(), itemToCheck);
                return;
            }
        }

        // Add more for loops if add more heads
    }
}
