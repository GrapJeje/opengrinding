package nl.grapjeje.opengrinding.core.guis;

import net.kyori.adventure.text.Component;
import nl.grapjeje.core.gui.Gui;
import nl.grapjeje.core.gui.GuiButton;
import nl.grapjeje.core.items.Item;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.api.Jobs;
import nl.grapjeje.opengrinding.utils.guis.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public class JobsMenu extends Menu {

    @Override
    public void open(Player player) {
        Gui.Builder builder = Gui.builder(InventoryType.CHEST, Component.text("Pickaxe Shop"));
        builder.withSize(27);

        boolean isAdmin = player.hasPermission("opengrinding.admin.jobs");

        OpenGrinding.getInstance()
                .getJobEnableStatuses()
                .forEach((job, enabled) -> {
                    GuiButton btn = new GuiButton(
                            Item.from(this.getMaterialForJob(job)).toBukkit(),
                            null
                    );
                });
    }

    private Material getMaterialForJob(Jobs job) {
        return switch (job) {
            case MINING -> Material.STONE_PICKAXE;
            case FISHING -> Material.FISHING_ROD;
            case LUMBER -> Material.IRON_AXE;
            case MAILMAN -> Material.CHEST;
            case FARMING -> Material.WHEAT;
            default -> Material.BARRIER;
        };
    }
}
