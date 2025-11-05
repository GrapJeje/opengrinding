package nl.grapjeje.opengrinding.jobs.core.listeners;

import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.farming.objects.Plant;
import nl.grapjeje.opengrinding.jobs.lumber.objects.Wood;
import nl.grapjeje.opengrinding.jobs.mailman.MailmanModule;
import nl.grapjeje.opengrinding.jobs.mining.objects.Ore;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.event.inventory.ClickType;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

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

        for (Wood wood : Wood.values()) {
            UUID id = null;

            String itemName = meta.hasDisplayName() ? meta.getDisplayName().toLowerCase() : "";
            if (itemName.contains("bark"))
                id = wood.getBarkUUID();
            else if (itemName.contains("wood"))
                id = wood.getWoodUUID();
            if (id == null) continue;

            if (meta.getPlayerProfile().getId().equals(id)) {
                e.setCancelled(true);
                if (shiftClick) player.getInventory().setItem(e.getSlot(), itemToCheck);
                return;
            }
        }

        UUID mailmanId = UUID.nameUUIDFromBytes(MailmanModule.getPackageUrl().getBytes(StandardCharsets.UTF_8));
        if (meta.getPlayerProfile().getId().equals(mailmanId)) {
            e.setCancelled(true);
            if (shiftClick) player.getInventory().setItem(e.getSlot(), itemToCheck);
            return;
        }

        for (Plant plant : Plant.values()) {
            if (meta.getPlayerProfile().getId().equals(plant.getUuid())) {
                e.setCancelled(true);
                if (shiftClick) player.getInventory().setItem(e.getSlot(), itemToCheck);
                return;
            }
        }

        // Add more for loops if add more heads
    }
}
