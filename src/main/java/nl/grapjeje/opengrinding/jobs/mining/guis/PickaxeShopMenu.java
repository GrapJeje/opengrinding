package nl.grapjeje.opengrinding.jobs.mining.guis;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nl.grapjeje.core.gui.Gui;
import nl.grapjeje.core.gui.GuiButton;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration.Pickaxe;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.grapjeje.opengrinding.utils.guis.ShopMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PickaxeShopMenu extends ShopMenu {
    private static final int[] SLOT_POSITIONS_1 = {13};
    private static final int[] SLOT_POSITIONS_2 = {12, 14};
    private static final int[] SLOT_POSITIONS_3 = {11, 13, 15};
    private static final int[] SLOT_POSITIONS_4 = {11, 12, 14, 15};

    @Override
    public void open(Player player) {
        PlayerGrindingModel model = GrindingPlayer.loadOrCreatePlayerModel(player, Jobs.MINING);
        int playerLevel = model.getLevel();

        Gui.Builder builder = Gui.builder(InventoryType.CHEST, Component.text("Pickaxe Shop"));
        builder.withSize(27);

        MiningJobConfiguration config = MiningModule.getConfig();
        List<Pickaxe> unlockedPickaxes = this.getUnlockedPickaxes(playerLevel, config);
        int[] slots = this.getSlotPositions(unlockedPickaxes.size());

        for (int i = 0; i < unlockedPickaxes.size(); i++) {
            Pickaxe pickaxe = unlockedPickaxes.get(i);
            GuiButton button = GuiButton.builder()
                    .withMaterial(this.getMaterialFromPickaxe(pickaxe.name()))
                    .withName(this.getPickaxeName(pickaxe.name()))
                    .withLore(
                            MessageUtil.filterMessage("<gray>Prijs: <bold>" + pickaxe.price() + "<!bold>"),
                            MessageUtil.filterMessage("<green>Klik om te kopen!")
                            )
                    .withClickEvent((gui, p, type) -> {
                        ItemStack item = new ItemStack(this.getMaterialFromPickaxe(pickaxe.name()));
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) meta.displayName(this.getPickaxeName(pickaxe.name()));
                        item.setItemMeta(meta);

                        String pickaxeName = PlainTextComponentSerializer.plainText().serialize(this.getPickaxeName(pickaxe.name()));

                        double amount = pickaxe.price();
                        this.removeCash(player, amount, pickaxeName);

                        p.getInventory().addItem(item);
                        p.sendMessage(MessageUtil.filterMessage(
                                "<green>Je hebt een <bold>" + pickaxeName + "<!bold> <green>gekocht voor <bold>" + pickaxe.price() + "<!bold>!"
                        ));
                        gui.close(p);
                    })
                    .build();
            builder.withButton(slots[i], button);
        }

        for (int i = 0; i < 27; i++) {
            if (!this.contains(slots, i)) builder.withButton(i, GuiButton.getFiller());
        }

        Gui gui = builder.build();
        this.registerGui(gui);
        gui.open(player);
    }

    private List<Pickaxe> getUnlockedPickaxes(int playerLevel, MiningJobConfiguration config) {
        List<Pickaxe> unlocked = new ArrayList<>();
        config.getPickaxes().values().stream()
                .sorted(Comparator.comparingInt(Pickaxe::unlockLevel))
                .forEach(pickaxe -> {
                    if (playerLevel >= pickaxe.unlockLevel()) unlocked.add(pickaxe);
                });
        return unlocked;
    }

    private int[] getSlotPositions(int unlockedCount) {
        return switch (unlockedCount) {
            case 1 -> SLOT_POSITIONS_1;
            case 2 -> SLOT_POSITIONS_2;
            case 3 -> SLOT_POSITIONS_3;
            default -> SLOT_POSITIONS_4;
        };
    }

    private Material getMaterialFromPickaxe(String pickaxe) {
        return switch (pickaxe.toLowerCase()) {
            case "stone" -> Material.STONE_PICKAXE;
            case "iron" -> Material.IRON_PICKAXE;
            case "diamond" -> Material.DIAMOND_PICKAXE;
            case "netherite" -> Material.NETHERITE_PICKAXE;
            default -> Material.WOODEN_PICKAXE;
        };
    }

    private Component getPickaxeName(String pickaxe) {
        return switch (pickaxe.toLowerCase()) {
            case "stone" -> MessageUtil.filterMessage("<gray>Stone Pickaxe");
            case "iron" -> MessageUtil.filterMessage("<white>Iron Pickaxe");
            case "diamond" -> MessageUtil.filterMessage("<aqua>Diamond Pickaxe");
            case "netherite" -> MessageUtil.filterMessage("<dark_gray>Netherite Pickaxe");
            default -> Component.text(MessageUtil.capitalizeWords(pickaxe) + " Pickaxe");
        };
    }
}
