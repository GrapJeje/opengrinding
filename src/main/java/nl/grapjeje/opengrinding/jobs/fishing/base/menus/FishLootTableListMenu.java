package nl.grapjeje.opengrinding.jobs.fishing.base.menus;

import net.kyori.adventure.text.Component;
import nl.grapjeje.core.gui.Gui;
import nl.grapjeje.core.gui.GuiButton;
import nl.grapjeje.opengrinding.jobs.core.gui.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class FishLootTableListMenu extends Menu {
    private final Gui gui;

    public FishLootTableListMenu(List<String> values, int page) {
        this.gui = Gui.builder(org.bukkit.event.inventory.InventoryType.CHEST, Component.text("FishLootTables - Page " + page))
                .withSize(54)
                .build();

        int start = (page - 1) * 45;
        int end = Math.min(start + 45, values.size());

        for (int i = start; i < end; i++) {
            String value = values.get(i);

            GuiButton button = GuiButton.builder()
                    .withMaterial(Material.NAME_TAG)
                    .withName(Component.text(value))
                    .withClickEvent((gui, player, type) -> {
                        player.performCommand("fishloottable open " + value);
                    })
                    .build();
            gui.setButton(i - start, button);
        }
    }

    @Override
    public void open(Player player) {
        this.registerGui(gui);
        gui.open(player);
    }
}
