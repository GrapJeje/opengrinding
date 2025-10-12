package nl.grapjeje.opengrinding.utils.guis;

import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.utils.JobModule;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;
import nl.openminetopia.OpenMinetopia;
import nl.openminetopia.configuration.MessageConfiguration;
import nl.openminetopia.modules.banking.BankingModule;
import nl.openminetopia.modules.transactions.TransactionsModule;
import nl.openminetopia.modules.transactions.enums.TransactionType;
import nl.openminetopia.modules.transactions.events.TransactionUpdateEvent;
import nl.openminetopia.utils.ChatUtils;
import nl.openminetopia.utils.events.EventUtils;
import org.bukkit.entity.Player;

public abstract class ShopMenu extends Menu {

    public void open(Player player, Class<? extends JobModule> moduleClass) {
        try {
            JobModule module = OpenGrinding.getFramework().getModuleLoader()
                    .getModules().stream()
                    .filter(moduleClass::isInstance)
                    .map(m -> (JobModule) m)
                    .findFirst()
                    .orElse(null);
            if (module == null) {
                player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Deze module is niet gevonden!"));
                return;
            }
            if (module.isDisabled()) {
                player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Deze module is momenteel uitgeschakeld!"));
                return;
            }
            JobConfig config = module.getJobConfig();
            if (!config.isEnabled()) {
                player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Deze module is uitgeschakeld in de config!"));
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.open(player);
    }

    protected void removeCash(Player player, double amount, String name) {
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

    protected boolean contains(int[] arr, int val) {
        for (int i : arr) if (i == val) return true;
        return false;
    }
}
