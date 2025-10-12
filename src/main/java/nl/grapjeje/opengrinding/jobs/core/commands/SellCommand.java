package nl.grapjeje.opengrinding.jobs.core.commands;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration;
import nl.grapjeje.opengrinding.jobs.mining.guis.ShopMenu;
import nl.grapjeje.opengrinding.jobs.mining.objects.Ore;
import nl.openminetopia.OpenMinetopia;
import nl.openminetopia.configuration.MessageConfiguration;
import nl.openminetopia.modules.banking.BankingModule;
import nl.openminetopia.modules.transactions.TransactionsModule;
import nl.openminetopia.modules.transactions.enums.TransactionType;
import nl.openminetopia.modules.transactions.events.TransactionUpdateEvent;
import nl.openminetopia.utils.ChatUtils;
import nl.openminetopia.utils.events.EventUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class SellCommand implements Command {

    @Override
    public String getName() {
        return "grindingsell";
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        Player player = source.getPlayer();
        if (player == null) {
            source.getSender().sendRichMessage("<warning>⚠ Alleen spelers kunnen dit commando gebruiken!");
            return;
        }

        if (args == null || args.length == 0) return;

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "mining" -> this.handleMiningCommand(player);
        }
    }

    private void handleMiningCommand(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() != Material.PLAYER_HEAD) {
            if (MiningModule.getConfig().isOpenBuyShop()) new ShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }

        SkullMeta meta = (SkullMeta) itemInHand.getItemMeta();
        if (meta == null || meta.getPlayerProfile() == null) {
            if (MiningModule.getConfig().isOpenBuyShop()) new ShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }

        UUID skullUuid = meta.getPlayerProfile().getId();

        MiningModule miningModule = OpenGrinding.getFramework().getModuleLoader()
                .getModules().stream()
                .filter(m -> m instanceof MiningModule)
                .map(m -> (MiningModule) m)
                .findFirst()
                .orElse(null);

        if (miningModule == null || miningModule.isDisabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ De mining module is momenteel uitgeschakeld!"));
            return;
        }

        MiningJobConfiguration config = MiningModule.getConfig();
        if (!config.isSellEnabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je kunt momenteel niks verkopen! Contacteer een beheerder als jij denkt dat dit een fout is."));
            return;
        }

        Optional<Ore> enumOre = Arrays.stream(Ore.values()).filter(ore -> ore.getUuid().equals(skullUuid)).findFirst();
        if (enumOre.isEmpty()) {
            if (MiningModule.getConfig().isOpenBuyShop()) new ShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }

        var oreRecord = config.getOres().values().stream()
                .filter(record -> Objects.equals(record.name(), enumOre.get().name().toLowerCase()))
                .findFirst()
                .orElse(null);

        if (oreRecord == null || oreRecord.sellPrice() <= 0) {
            if (MiningModule.getConfig().isOpenBuyShop()) new ShopMenu().open(player);
            else player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }

        double pricePerOne = oreRecord.sellPrice();

        double amount = pricePerOne * itemInHand.getAmount();
        this.giveCash(player, amount);

        player.getInventory().removeItem(itemInHand);
        String itemName = PlainTextComponentSerializer.plainText()
                .serialize(Objects.requireNonNull(itemInHand.getItemMeta().displayName()));

        player.sendMessage(MessageUtil.filterMessage(
                "<green>Je hebt succesvol <bold>" + itemInHand.getAmount() + " " + itemName +
                        "<!bold> verkocht voor <bold>" + amount + "<!bold>!"
        ));
    }

    private void giveCash(Player player, double amount) {
        BankingModule bankingModule = (BankingModule) OpenMinetopia.getModuleManager().get(BankingModule.class);
        bankingModule.getAccountByNameAsync(player.getName()).whenComplete((accountModel, throwable) -> {
            if (accountModel == null) {
                player.sendMessage(MessageConfiguration.component("banking_account_not_found"));
            } else {
                TransactionUpdateEvent event = new TransactionUpdateEvent(
                        player.getUniqueId(),
                        player.getName(),
                        TransactionType.DEPOSIT,
                        amount,
                        accountModel,
                        "Sold ores",
                        System.currentTimeMillis()
                );
                if (EventUtils.callCancellable(event)) {
                    player.sendMessage(ChatUtils.color("<red>De transactie is geannuleerd door een plugin."));
                } else {
                    accountModel.setBalance(accountModel.getBalance() + amount);
                    accountModel.save();
                    TransactionsModule transactionsModule =
                            (TransactionsModule) OpenMinetopia.getModuleManager().get(TransactionsModule.class);
                    transactionsModule.createTransactionLog(
                            System.currentTimeMillis(),
                            player.getUniqueId(),
                            player.getName(),
                            TransactionType.DEPOSIT,
                            amount,
                            accountModel.getUniqueId(),
                            "Sold ores"
                    );
                }
            }
        });
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, @NotNull String[] args) {
        Player player = source.getPlayer();
        if (player == null) return Collections.emptyList();
        if (!this.canUse(player)) return Collections.emptyList();

        if (args.length == 1) {
            return Stream.of("mining")
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }

    @Override
    public @Nullable String permission() {
        return "opengrinding.*";
    }
}
