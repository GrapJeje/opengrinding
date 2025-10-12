package nl.grapjeje.opengrinding.jobs.fishing.guis;

import net.kyori.adventure.text.Component;
import nl.grapjeje.core.gui.Gui;
import nl.grapjeje.core.gui.GuiButton;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.jobs.fishing.FishingModule;
import nl.grapjeje.opengrinding.jobs.fishing.configuration.FishingJobConfiguration;
import nl.grapjeje.opengrinding.utils.guis.ShopMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FishingRodShopMenu extends ShopMenu {
    @Override
    public void open(Player player) {
        Gui.Builder builder = Gui.builder(InventoryType.CHEST, Component.text("Pickaxe Shop"));
        builder.withSize(27);

        FishingJobConfiguration config = FishingModule.getConfig();
        double rodPrice = config.getRods().get(Material.FISHING_ROD.name().toLowerCase()).buyPrice();

        GuiButton button = GuiButton.builder()
                .withMaterial(Material.FISHING_ROD)
                .withName(MessageUtil.filterMessage("<!italic><gray>Vishengel"))
                .withLore(
                        MessageUtil.filterMessage("<gray>Prijs: <bold>" + rodPrice + "<!bold>"),
                        MessageUtil.filterMessage("<green>Klik om te kopen!")
                )
                .withClickEvent((gui, p, type) -> {
                    ItemStack item = new ItemStack(Material.FISHING_ROD);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) meta.displayName(MessageUtil.filterMessage("<gray>Vishengel"));
                    item.setItemMeta(meta);

                    String rodName = "<!italic><gray>Vishengel";

                    this.removeCash(player, rodPrice, "Vishengel");

                    p.getInventory().addItem(item);
                    p.sendMessage(MessageUtil.filterMessage(
                            "<green>Je hebt een <bold>" + rodName + "<!bold> <green>gekocht voor <bold>" + rodPrice + "<!bold>!"
                    ));
                    gui.close(p);
                })
                .build();
        builder.withButton(13, button);

        for (int i = 0; i < 27; i++) {
            if (i != 13) builder.withButton(i, GuiButton.getFiller());
        }

        Gui gui = builder.build();
        this.registerGui(gui);
        gui.open(player);
    }
}
