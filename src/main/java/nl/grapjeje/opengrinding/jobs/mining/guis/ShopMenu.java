package nl.grapjeje.opengrinding.jobs.mining.guis;

import com.craftmend.storm.api.enums.Where;
import net.kyori.adventure.text.Component;
import nl.grapjeje.core.gui.Gui;
import nl.grapjeje.core.gui.GuiButton;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.core.gui.Menu;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.*;

public class ShopMenu extends Menu {
    private static final int[] SLOT_POSITIONS_1 = {13};
    private static final int[] SLOT_POSITIONS_2 = {12, 14};
    private static final int[] SLOT_POSITIONS_3 = {11, 13, 15};
    private static final int[] SLOT_POSITIONS_4 = {11, 12, 14, 15};

    @Override
    public void open(Player player) {
        PlayerGrindingModel model = this.loadOrCreatePlayerModel(player);
        int playerLevel = model.getLevel();

        MiningJobConfiguration config = MiningModule.getConfig();
        Gui.Builder builder = Gui.builder(InventoryType.CHEST, Component.text("Pickaxe Shop"));
        builder.withSize(27);

        List<String> unlockedPickaxes = this.getUnlockedPickaxes(playerLevel, config);
        int[] slots = this.getSlotPositions(unlockedPickaxes.size());

        for (int i = 0; i < unlockedPickaxes.size(); i++) {
            String pickaxe = unlockedPickaxes.get(i);
//            double price = config.getBuyPrices().get("pickaxe").get(pickaxe);
            double price = 0.0;
            GuiButton button = GuiButton.builder()
                    .withMaterial(this.getMaterialFromPickaxe(pickaxe))
                    .withName(this.getColoredPickaxeName(pickaxe))
                    .withLore(
                            Component.text("Prijs: " + price + " punten"),
                            Component.text("Klik om te kopen!")
                    )
                    .withClickEvent((gui, p, type) -> {
                        p.getInventory().addItem(new org.bukkit.inventory.ItemStack(this.getMaterialFromPickaxe(pickaxe)));
                        p.sendMessage(Component.text("Je hebt een " + pickaxe + " gekocht voor " + price + " punten!"));
                        gui.close(p);
                    })
                    .build();

            builder.withButton(slots[i], button);
        }

        // Filler voor lege slots
        for (int i = 0; i < 27; i++) {
            if (!this.contains(slots, i)) builder.withButton(i, GuiButton.getFiller());
        }

        Gui gui = builder.build();
        this.registerGui(gui);
        gui.open(player);
    }

    private List<String> getUnlockedPickaxes(int playerLevel, MiningJobConfiguration config) {
//        Map<String, Double> buyables = config.getBuyPrices().get("pickaxe");
//        Map<String, Integer> unlocks = config.getPickaxeUnlockLevels();
        Map<String, Double> buyables = new HashMap<>();
        Map<String, Integer> unlocks = new HashMap<>();
        List<String> unlocked = new ArrayList<>();

        Bukkit.getLogger().info("==== DEBUG: Player level = " + playerLevel + " ====");

        buyables.keySet().stream()
                .sorted(Comparator.comparingInt(a -> unlocks.getOrDefault(a, 0)))
                .forEach(pickaxe -> {
                    int requiredLevel = unlocks.getOrDefault(pickaxe, 0);
                    Bukkit.getLogger().info("Pickaxe: " + pickaxe + ", requiredLevel: " + requiredLevel);
                    if (playerLevel >= requiredLevel) {
                        unlocked.add(pickaxe);
                        Bukkit.getLogger().info("-> Unlocked!");
                    } else {
                        Bukkit.getLogger().info("-> Locked!");
                    }
                });

        Bukkit.getLogger().info("Unlocked pickaxes: " + unlocked);
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
            case "wooden" -> Material.WOODEN_PICKAXE;
            case "stone" -> Material.STONE_PICKAXE;
            case "iron" -> Material.IRON_PICKAXE;
            case "golden" -> Material.GOLDEN_PICKAXE;
            case "diamond" -> Material.DIAMOND_PICKAXE;
            case "netherite" -> Material.NETHERITE_PICKAXE;
            default -> Material.WOODEN_PICKAXE;
        };
    }

    private Component getColoredPickaxeName(String pickaxe) {
        return switch (pickaxe.toLowerCase()) {
            case "stone" -> Component.text("§7Stone Pickaxe");
            case "iron" -> Component.text("§fIron Pickaxe");
            case "golden" -> Component.text("§6Golden Pickaxe");
            case "diamond" -> Component.text("§bDiamond Pickaxe");
            case "netherite" -> Component.text("§8Netherite Pickaxe");
            default -> Component.text(MessageUtil.capitalizeWords(pickaxe) + " Pickaxe");
        };
    }

    private boolean contains(int[] arr, int val) {
        for (int i : arr) if (i == val) return true;
        return false;
    }

    private PlayerGrindingModel loadOrCreatePlayerModel(Player player) {
        if (CoreModule.getPlayerCache().containsKey(player.getUniqueId()))
            return CoreModule.getPlayerCache().get(player.getUniqueId());

        Optional<PlayerGrindingModel> playerModelOpt;
        try {
            playerModelOpt = StormDatabase.getInstance().getStorm()
                    .buildQuery(PlayerGrindingModel.class)
                    .where("player_uuid", Where.EQUAL , player.getUniqueId())
                    .where("job_name", Where.EQUAL, Jobs.MINING.name())
                    .limit(1)
                    .execute()
                    .join()
                    .stream()
                    .findFirst();
        } catch (Exception ex) {
            ex.printStackTrace();
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                    player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het ophalen van jouw spelersdata!"))
            );
            throw new RuntimeException(ex);
        }

        return playerModelOpt.orElseGet(() -> {
            PlayerGrindingModel m = new PlayerGrindingModel();
            m.setPlayerUuid(player.getUniqueId());
            m.setJob(Jobs.MINING);
            m.setLevel(0);
            m.setValue(0.0);
            Bukkit.getLogger().info("New player grind model made for " + player.getName());
            return m;
        });
    }
}
