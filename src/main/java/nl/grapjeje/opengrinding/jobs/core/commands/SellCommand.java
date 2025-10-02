package nl.grapjeje.opengrinding.jobs.core.commands;

import com.craftmend.storm.api.enums.Where;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.mining.objects.Ore;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.openminetopia.OpenMinetopia;
import nl.openminetopia.api.player.PlayerManager;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import nl.openminetopia.configuration.MessageConfiguration;
import nl.openminetopia.modules.banking.BankingModule;
import nl.openminetopia.modules.currencies.models.CurrencyModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
import nl.openminetopia.modules.player.models.PlayerModel;
import nl.openminetopia.modules.transactions.TransactionsModule;
import nl.openminetopia.modules.transactions.enums.TransactionType;
import nl.openminetopia.modules.transactions.events.TransactionUpdateEvent;
import nl.openminetopia.utils.ChatUtils;
import nl.openminetopia.utils.events.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class SellCommand implements Command {

    @Override
    public String getName() {
        return "sell";
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
//            default -> this.sendHelp(player);
        }
    }

    private void handleMiningCommand(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() != Material.PLAYER_HEAD) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }

        SkullMeta meta = (SkullMeta) itemInHand.getItemMeta();
        if (meta == null || meta.getPlayerProfile() == null) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }
        UUID skullUuid = meta.getPlayerProfile().getId();
        Ore matchedOre = null;

        for (Ore ore : Ore.values()) {
            if (ore.getUuid().equals(skullUuid)) {
                matchedOre = ore;
                break;
            }
        }
        if (matchedOre == null) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Dit item kun je hier niet verkopen!"));
            return;
        }

        if (!CoreModule.getGrindingShopConfiguration().getShops().get("mining").sellEnabled()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je kunt momenteel niks verkopen! Contacteer een beheerder als jij denkt dat dit een fout is."));
            return;
        }

        double pricePerOne = CoreModule.getGrindingShopConfiguration().getShops().get("mining").sell().getOrDefault(matchedOre.name().toLowerCase(), -1.0);
        double amount = pricePerOne * itemInHand.getAmount();
        this.giveCash(player, amount);

        player.getInventory().removeItem(itemInHand);
        String itemName = PlainTextComponentSerializer.plainText().serialize(itemInHand.getItemMeta().displayName());
        player.sendMessage(MessageUtil.filterMessage("<green>Je hebt succesvol <bold>" + itemInHand.getAmount() + " " + itemName + "<!bold> verkocht voor <bold>" + amount + "<!bold>!"));
    }

    private void giveCash(Player player, double amount) {
        BankingModule bankingModule = (BankingModule) OpenMinetopia.getModuleManager().get(BankingModule.class);
        bankingModule.getAccountByNameAsync(player.getName()).whenComplete((accountModel, throwable) -> {
            if (accountModel == null) {
                player.sendMessage(MessageConfiguration.component("banking_account_not_found"));
            } else {
                TransactionUpdateEvent event = new TransactionUpdateEvent(player.getUniqueId(), player.getName(), TransactionType.SET, amount, accountModel, "Sold ores", System.currentTimeMillis());
                if (EventUtils.callCancellable(event)) {
                    player.sendMessage(ChatUtils.color("<red>De transactie is geannuleerd door een plugin."));
                } else {
                    accountModel.setBalance(accountModel.getBalance() + amount);
                    accountModel.save();
                    TransactionsModule transactionsModule = (TransactionsModule)OpenMinetopia.getModuleManager().get(TransactionsModule.class);
                    transactionsModule.createTransactionLog(System.currentTimeMillis(), player.getUniqueId(), player.getName(), TransactionType.SET, amount, accountModel.getUniqueId(), "Sold ores");
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
