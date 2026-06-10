package nl.grapjeje.opengrinding.jobs.mailman.listeners;

import nl.grapjeje.opengrinding.jobs.mailman.objects.MailmanJob;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class ItemListener implements Listener {

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        Material offHandItem = player.getInventory().getItemInOffHand().getType();

        if (!MailmanJob.isActive(player)) return;
        if (offHandItem != Material.AIR &&
                e.getItemDrop().getItemStack().equals(player.getInventory().getItemInOffHand())) {
            e.setCancelled(true);

            player.getInventory().setItemInOffHand(e.getItemDrop().getItemStack());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        int slot = e.getRawSlot();

        if (!MailmanJob.isActive(player)) return;
        if (slot == 45) e.setCancelled(true);
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        if (!MailmanJob.isActive(player)) return;
        e.setCancelled(true);
    }
}
