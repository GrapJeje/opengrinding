package nl.grapjeje.opengrinding.jobs.fishing.base.menus;

import com.craftmend.storm.Storm;
import com.craftmend.storm.api.enums.Where;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nl.grapjeje.core.gui.Gui;
import nl.grapjeje.core.gui.GuiButton;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.utils.Menu;
import nl.grapjeje.opengrinding.jobs.fishing.base.objects.FishLootTable;
import nl.grapjeje.opengrinding.models.FishLootTableModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class FishLootTableMenu extends Menu {
    private final Gui gui;
    private final String value;
    private final List<FishLootTable> items;

    public FishLootTableMenu(String value, List<FishLootTable> items) {
        this.value = value;
        this.items = items;

        this.gui = Gui.builder(org.bukkit.event.inventory.InventoryType.CHEST, Component.text("LootTable: " + value))
                .withSize(54)
                .build();

        this.loadButtons();
    }

    @Override
    public void open(Player player) {
        this.registerGui(gui);
        gui.open(player);
    }

    private void loadButtons() {
        int i = 0;
        for (FishLootTable loot : items) {
            ItemStack display = loot.getItem();
            if (display == null) continue;
            ItemMeta meta = display.getItemMeta();
            if (meta == null) continue;

            GuiButton.Builder buttonBuilder = GuiButton.builder()
                    .withMaterial(display.getType())
                    .withName(meta.displayName());

            List<Component> lore = new ArrayList<>();
            if (meta.hasLore()) {
                lore.addAll(meta.lore());
                lore.add(Component.text(" "));
            }

            lore.add(MessageUtil.filterMessage("<gray>Chance: <white>" + loot.getChance() + "%"));
            lore.add(MessageUtil.filterMessage("<gray>Right click <dark_gray>- <white>Verwijder item uit loot table"));
            lore.add(MessageUtil.filterMessage("<gray>Left click <dark_gray>- <white>Verkrijg item in hand"));
            buttonBuilder.withLore(lore);

            buttonBuilder.withClickEvent((gui, player, type) -> {
                if (type.isLeftClick()) {
                    player.getInventory().addItem(display.clone());
                    player.closeInventory();
                } else if (type.isRightClick()) {
                    Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () -> {
                        Storm storm = StormDatabase.getInstance().getStorm();
                        //  TODO: Fix delete
                        if (display.getItemMeta() != null) {
                            String itemMetaJson = FishLootTableModel.getGSON().toJson(display.getItemMeta());
                            try {
                                Collection<FishLootTableModel> models = storm.buildQuery(FishLootTableModel.class)
                                        .where("metadata", Where.EQUAL, itemMetaJson)
                                        .execute()
                                        .get();
                                for (FishLootTableModel model : models) {
                                    storm.delete(model);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            Material mat = display.getType();
                            try {
                                Collection<FishLootTableModel> models = storm.buildQuery(FishLootTableModel.class)
                                        .where("material", Where.EQUAL, mat.toString())
                                        .execute()
                                        .get();
                                FishLootTableModel toDelete = models.stream()
                                        .filter(m -> m.getMetadata() == null || m.getMetadata().isEmpty())
                                        .findFirst()
                                        .orElse(null);
                                if (toDelete == null && !models.isEmpty()) toDelete = models.iterator().next();
                                if (toDelete != null) storm.delete(toDelete);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        String itemName = PlainTextComponentSerializer.plainText()
                                .serialize(Objects.requireNonNull(display.getItemMeta().displayName()));
                        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                            player.sendMessage(MessageUtil.filterMessage("<warning><bold>" + itemName + "<!bold> verwijderd uit <bold>" + value + "<!bold>!"));
                            player.closeInventory();
                        });
                    });
                }
            });

            GuiButton button = buttonBuilder.build();
            gui.setButton(i++, button);
        }
    }
}



