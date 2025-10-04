package nl.grapjeje.opengrinding.utils.currency;

import com.craftmend.storm.api.enums.Where;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.models.CurrencyModel;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingCurrency;
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
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CurrencyUtil {

    public static CompletableFuture<Map<Currency, Double>> giveReward(Player player, double cashAmount, double grindTokens, String reason) {
        checkIfNeedReset(player);
        if (CoreModule.getConfig().isSellInTokens())
            return giveTokens(player, grindTokens);
        else
            return giveCash(player, cashAmount, reason);
    }

    // TODO: Check if the player has enough

    public static CompletableFuture<Map<Currency, Double>> removeForBuy(Player player, double amount, String reason) {
        if (CoreModule.getConfig().isBuyInTokens()) {
            return getModelAsync(player).thenApply(optional -> {
                if (optional.isEmpty()) return new HashMap<Currency, Double>();
                CurrencyModel model = optional.get();
                GrindingCurrency currency = new GrindingCurrency(player.getUniqueId(), model);
                currency.getModel().setGrindTokens(currency.getModel().getGrindTokens() - amount);
                currency.save();

                return (Map<Currency, Double>) (Map<?, ?>) Map.of(Currency.TOKENS, amount);
            });
        } else {
            CompletableFuture<Map<Currency, Double>> future = new CompletableFuture<>();
            BankingModule bankingModule = (BankingModule) OpenMinetopia.getModuleManager().get(BankingModule.class);

            bankingModule.getAccountByNameAsync(player.getName()).whenComplete((accountModel, throwable) -> {
                if (accountModel == null) {
                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                            player.sendMessage(MessageConfiguration.component("banking_account_not_found"))
                    );
                    future.complete(new HashMap<Currency, Double>());
                } else {
                    TransactionUpdateEvent event = new TransactionUpdateEvent(
                            player.getUniqueId(),
                            player.getName(),
                            TransactionType.WITHDRAW,
                            amount,
                            accountModel,
                            "Bought " + reason,
                            System.currentTimeMillis()
                    );

                    if (EventUtils.callCancellable(event)) {
                        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                                player.sendMessage(ChatUtils.color("<red>De transactie is geannuleerd door een plugin."))
                        );
                        future.complete(new HashMap<Currency, Double>());
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
                                "Bought " + reason
                        );

                        future.complete((Map<Currency, Double>) (Map<?, ?>) Map.of(Currency.CASH, amount));
                    }
                }
            });

            return future;
        }
    }

    private static double clampToLimit(CurrencyModel model, Currency type, double amount) {
        double current;
        double max;
        if (type == Currency.TOKENS) {
            current = model.getTokensFromToday();
            max = CoreModule.getConfig().getTokenLimit();
        } else {
            current = model.getCashFromToday();
            max = CoreModule.getConfig().getCashLimit();
        }
        if (current >= max) return 0.0;
        return Math.min(amount, max - current);
    }

    private static CompletableFuture<Map<Currency, Double>> giveCash(Player player, double amount, String reason) {
        return getModelAsync(player).thenApply(optional -> {
            if (optional.isEmpty()) return new HashMap<Currency, Double>();
            CurrencyModel model = optional.get();
            GrindingCurrency currency = new GrindingCurrency(player.getUniqueId(), model);

            double allowed = clampToLimit(currency.getModel(), Currency.CASH, amount);
            if (allowed <= 0) {
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Jij hebt jouw grinding cash limiet bereikt!"))
                );
                return (Map<Currency, Double>) (Map<?, ?>) Map.of(currency, 0.0);
            }
            currency.getModel().setCashFromToday(currency.getModel().getCashFromToday() + allowed);
            currency.getModel().setLastUpdated(LocalDate.now());
            currency.save();

            BankingModule bankingModule = (BankingModule) OpenMinetopia.getModuleManager().get(BankingModule.class);
            bankingModule.getAccountByNameAsync(player.getName()).whenComplete((accountModel, throwable) -> {
                if (accountModel == null) {
                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                            player.sendMessage(MessageConfiguration.component("banking_account_not_found"))
                    );
                    return;
                }

                TransactionUpdateEvent event = new TransactionUpdateEvent(
                        player.getUniqueId(),
                        player.getName(),
                        TransactionType.DEPOSIT,
                        allowed,
                        accountModel,
                        reason,
                        System.currentTimeMillis()
                );

                if (EventUtils.callCancellable(event)) {
                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                            player.sendMessage(ChatUtils.color("<red>De transactie is geannuleerd door een plugin."))
                    );
                    return;
                }
                accountModel.setBalance(accountModel.getBalance() + allowed);
                accountModel.save();

                TransactionsModule transactionsModule =
                        (TransactionsModule) OpenMinetopia.getModuleManager().get(TransactionsModule.class);
                transactionsModule.createTransactionLog(
                        System.currentTimeMillis(),
                        player.getUniqueId(),
                        player.getName(),
                        TransactionType.DEPOSIT,
                        allowed,
                        accountModel.getUniqueId(),
                        reason
                );
            });
            return (Map<Currency, Double>) (Map<?, ?>) Map.of(currency, allowed);
        });
    }

    private static CompletableFuture<Map<Currency, Double>> giveTokens(Player player, double amount) {
        return getModelAsync(player).thenApply(optional -> {
            if (optional.isEmpty()) return new HashMap<Currency, Double>();
            CurrencyModel model = optional.get();
            GrindingCurrency currency = new GrindingCurrency(player.getUniqueId(), model);

            double allowed = clampToLimit(currency.getModel(), Currency.TOKENS, amount);
            if (allowed <= 0) {
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Jij hebt jouw grindtoken limiet bereikt!"))
                );
                return (Map<Currency, Double>) (Map<?, ?>) Map.of(currency, 0.0);
            }
            currency.getModel().setGrindTokens(currency.getModel().getGrindTokens() + allowed);
            currency.getModel().setTokensFromToday(currency.getModel().getTokensFromToday() + allowed);
            currency.getModel().setLastUpdated(LocalDate.now());
            currency.save();
            return (Map<Currency, Double>) (Map<?, ?>) Map.of(currency, allowed);
        });
    }

    private static CompletableFuture<Optional<CurrencyModel>> getModelAsync(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return StormDatabase.getInstance().getStorm()
                        .buildQuery(CurrencyModel.class)
                        .where("player_uuid", Where.EQUAL, player.getUniqueId())
                        .limit(1)
                        .execute()
                        .join()
                        .stream()
                        .findFirst();
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het ophalen van jouw player data!"))
                );
                return Optional.empty();
            }
        });
    }

    private static void checkIfNeedReset(Player player) {
        getModelAsync(player).thenAccept(optional -> {
            if (optional.isEmpty()) return;
            CurrencyModel model = optional.get();
            GrindingCurrency currency = new GrindingCurrency(player.getUniqueId(), model);
            currency.checkIfNeedsReset();
        });
    }
}
