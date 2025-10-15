package nl.grapjeje.opengrinding.jobs.lumber.guis;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nl.grapjeje.core.gui.Gui;
import nl.grapjeje.core.gui.GuiButton;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingPlayer;
import nl.grapjeje.opengrinding.jobs.lumber.LumberModule;
import nl.grapjeje.opengrinding.jobs.lumber.configuration.LumberJobConfiguration;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.grapjeje.opengrinding.utils.currency.CurrencyUtil;
import nl.grapjeje.opengrinding.utils.currency.Price;
import nl.grapjeje.opengrinding.utils.guis.ShopMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AxeShopMenu extends ShopMenu {
    private static final int[] SLOT_POSITIONS_1 = {13};
    private static final int[] SLOT_POSITIONS_2 = {12, 14};
    private static final int[] SLOT_POSITIONS_3 = {11, 13, 15};
    private static final int[] SLOT_POSITIONS_4 = {11, 12, 14, 15};

    @Override
    public void open(Player player) {
        PlayerGrindingModel model = GrindingPlayer.loadOrCreatePlayerModel(player, Jobs.LUMBER);
        int playerLevel = model.getLevel();

        Gui.Builder builder = Gui.builder(InventoryType.CHEST, Component.text("Pickaxe Shop"));
        builder.withSize(27);

        LumberJobConfiguration config = LumberModule.getConfig();
        List<LumberJobConfiguration.Axe> unlockedAxes = this.getUnlockedAxes(playerLevel, config);
        int[] slots = this.getSlotPositions(unlockedAxes.size());

        for (int i = 0; i < unlockedAxes.size(); i++) {
            LumberJobConfiguration.Axe axe = unlockedAxes.get(i);
            Price price = axe.price();
            double amount;
            String currencyType;

            if (CoreModule.getConfig().isBuyInTokens()) {
                amount = price.grindToken();
                currencyType = " Tokens";
            } else {
                currencyType = "";
                amount = price.cash();
            }

            GuiButton button = GuiButton.builder()
                    .withMaterial(this.getMaterialFromAxe(axe.name()))
                    .withName(this.getAxeName(axe.name()))
                    .withLore(
                            MessageUtil.filterMessage("<gray>Prijs: <bold>" + amount + currencyType + "<!bold>"),
                            MessageUtil.filterMessage("<green>Klik om te kopen!")
                    )
                    .withClickEvent((gui, p, type) -> {
                        ItemStack item = new ItemStack(this.getMaterialFromAxe(axe.name()));
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) meta.displayName(this.getAxeName(axe.name()));
                        item.setItemMeta(meta);

                        String axeName = PlainTextComponentSerializer.plainText().serialize(this.getAxeName(axe.name()));

                        CurrencyUtil.removeForBuy(p, amount, axeName).thenAccept(map -> {
                            boolean success = !map.isEmpty();
                            if (success) {
                                p.getInventory().addItem(item);
                                p.sendMessage(MessageUtil.filterMessage(
                                        "<green>Je hebt een <bold>" + axeName + "<!bold> <green>gekocht voor <bold>" + amount + currencyType + "<!bold>!"
                                ));
                            }
                        });
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

    private List<LumberJobConfiguration.Axe> getUnlockedAxes(int playerLevel, LumberJobConfiguration config) {
        List<LumberJobConfiguration.Axe> unlocked = new ArrayList<>();
        config.getAxes().values().stream()
                .sorted(Comparator.comparingInt(LumberJobConfiguration.Axe::unlockLevel))
                .forEach(axe -> {
                    if (playerLevel >= axe.unlockLevel()) unlocked.add(axe);
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

    private Material getMaterialFromAxe(String axe) {
        return switch (axe.toLowerCase()) {
            case "stone" -> Material.STONE_AXE;
            case "iron" -> Material.IRON_AXE;
            case "diamond" -> Material.DIAMOND_AXE;
            case "netherite" -> Material.NETHERITE_AXE;
            default -> Material.WOODEN_AXE;
        };
    }

    private Component getAxeName(String axe) {
        return switch (axe.toLowerCase()) {
            case "stone" -> MessageUtil.filterMessage("<gray>Stone Axe");
            case "iron" -> MessageUtil.filterMessage("<white>Iron Axe");
            case "diamond" -> MessageUtil.filterMessage("<aqua>Diamond Axe");
            case "netherite" -> MessageUtil.filterMessage("<dark_gray>Netherite Axe");
            default -> Component.text(MessageUtil.capitalizeWords(axe) + " Axe");
        };
    }
}
