package nl.grapjeje.opengrinding.jobs.mining.guis;

import com.craftmend.storm.api.enums.Where;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nl.grapjeje.core.gui.Gui;
import nl.grapjeje.core.gui.GuiButton;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.utils.Menu;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration.Pickaxe;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.openminetopia.OpenMinetopia;
import nl.openminetopia.configuration.MessageConfiguration;
import nl.openminetopia.modules.banking.BankingModule;
import nl.openminetopia.modules.data.storm.StormDatabase;
import nl.openminetopia.modules.transactions.TransactionsModule;
import nl.openminetopia.modules.transactions.enums.TransactionType;
import nl.openminetopia.modules.transactions.events.TransactionUpdateEvent;
import nl.openminetopia.utils.ChatUtils;
import nl.openminetopia.utils.events.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

    private void removeCash(Player player, double amount, String name) {
        BankingModule bankingModule = (BankingModule) OpenMinetopia.getModuleManager().get(BankingModule.class);
        bankingModule.getAccountByNameAsync(player.getName()).whenComplete((accountModel, throwable) -> {
            if (accountModel == null) {
                player.sendMessage(MessageConfiguration.component("banking_account_not_found"));
            } else {
                TransactionUpdateEvent event = new TransactionUpdateEvent(
                        player.getUniqueId(),
                        player.getName(),
                        TransactionType.WITHDRAW,
                        amount,
                        accountModel,
                        "Bought " + name,
                        System.currentTimeMillis()
                );
                if (EventUtils.callCancellable(event)) {
                    player.sendMessage(ChatUtils.color("<red>De transactie is geannuleerd door een plugin."));
                } else {
                    accountModel.setBalance(accountModel.getBalance() - amount);
                    accountModel.save();
                    TransactionsModule transactionsModule =
                            (TransactionsModule) OpenMinetopia.getModuleManager().get(TransactionsModule.class);
                    transactionsModule.createTransactionLog(
                            System.currentTimeMillis(),
                            player.getUniqueId(),
                            player.getName(),
                            TransactionType.WITHDRAW,
                            amount,
                            accountModel.getUniqueId(),
                            "Bought " + name
                    );
                }
            }
        });
    }
}
