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
import nl.openminetopia.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CurrencyUtil {

    public static CompletableFuture<Map<Currency, Double>> giveReward(Player player, double cashAmount, double grindTokens, String reason) {
        checkIfNeedReset(player);

        if (CoreModule.getConfig().isSellInTokens()) {
            grindTokens = Math.floor(grindTokens * 1000) / 1000.0;
            return giveTokens(player, grindTokens);
        } else {
            cashAmount = Math.floor(cashAmount * 100) / 100.0;
            return giveCash(player, cashAmount, reason);
        }
    }

    public static CompletableFuture<Map<Currency, Double>> removeForBuy(Player player, double amount, String reason) {
        if (CoreModule.getConfig().isBuyInTokens()) {
            return getModelAsync(player).thenApply(model -> {
                GrindingCurrency currency = new GrindingCurrency(player.getUniqueId(), model);

                double currentTokens = currency.getModel().getGrindTokens();

                if (currentTokens < amount) {
                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Je hebt niet genoeg grindtokens!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                    });
                    return new HashMap<Currency, Double>();
                }

                currency.getModel().setGrindTokens(currentTokens - amount);
                currency.save();

                return (Map<Currency, Double>) (Map<?, ?>) Map.of(Currency.TOKENS, amount);
            });
        } else {
            CompletableFuture<Map<Currency, Double>> future = new CompletableFuture<>();
            BankingModule bankingModule = (BankingModule) OpenMinetopia.getModuleManager().get(BankingModule.class);

            bankingModule.getAccountByIdAsync(player.getUniqueId()).whenComplete((accountModel, throwable) -> {
                if (accountModel == null) {
                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                            player.sendMessage(MessageConfiguration.component("banking_account_not_found"))
                    );
                    future.complete(new HashMap<Currency, Double>());
                } else {
                    if (accountModel.getBalance() < amount) {
                        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                                player.sendMessage(ChatUtils.color("<warning>⚠ Je hebt niet genoeg saldo op je rekening."))
                        );
                        future.complete(new HashMap<Currency, Double>());
                        return;
                    }

                    accountModel.setBalance(accountModel.getBalance() - amount);
                    accountModel.save();

                    future.complete((Map<Currency, Double>) (Map<?, ?>) Map.of(Currency.CASH, amount));
                }
            });

            return future;
        }
    }

    private static double getRemainingForToday(CurrencyModel model, Currency type) {
        if (type == Currency.TOKENS) return CoreModule.getConfig().getTokenLimit() - model.getTokensFromToday();
        else return CoreModule.getConfig().getCashLimit() - model.getCashFromToday();
    }

    private static CompletableFuture<Map<Currency, Double>> giveCash(Player player, double amount, String reason) {
        return getModelAsync(player).thenApply(model -> {
            GrindingCurrency currency = new GrindingCurrency(player.getUniqueId(), model);

            double allowed;
            if (CoreModule.getConfig().isDailyLimit()) {
                allowed = getRemainingForToday(currency.getModel(), Currency.CASH);
                if (allowed <= amount) {
                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Jij hebt jouw grinding cash limiet bereikt!"))
                    );
                } else allowed = amount;
                if (allowed <= 0) return (Map<Currency, Double>) (Map<?, ?>) Map.of(currency, 0.0);
            } else allowed = amount;

            currency.getModel().setCashFromToday(currency.getModel().getCashFromToday() + allowed);
            currency.getModel().setLastUpdatedDate(LocalDate.now());
            currency.save();

            BankingModule bankingModule = (BankingModule) OpenMinetopia.getModuleManager().get(BankingModule.class);
            double finalAllowed = allowed;
            bankingModule.getAccountByIdAsync(player.getUniqueId()).whenComplete((accountModel, throwable) -> {
                if (accountModel == null) {
                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                            player.sendMessage(MessageConfiguration.component("banking_account_not_found"))
                    );
                    return;
                }
                accountModel.setBalance(accountModel.getBalance() + finalAllowed);
                accountModel.save();
            });
            return (Map<Currency, Double>) (Map<?, ?>) Map.of(currency, allowed);
        });
    }

    private static CompletableFuture<Map<Currency, Double>> giveTokens(Player player, double amount) {
        return getModelAsync(player).thenApply(model -> {
            GrindingCurrency currency = new GrindingCurrency(player.getUniqueId(), model);

            double allowed;
            if (CoreModule.getConfig().isDailyLimit()) {
                allowed = getRemainingForToday(currency.getModel(), Currency.TOKENS);
                if (allowed <= amount) {
                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Jij hebt jouw grindtoken limiet bereikt!"))
                    );
                } else allowed = amount;
                if (allowed <= 0) return (Map<Currency, Double>) (Map<?, ?>) Map.of(currency, 0.0);
            } else allowed = amount;

            currency.getModel().setGrindTokens(currency.getModel().getGrindTokens() + allowed);
            currency.getModel().setTokensFromToday(currency.getModel().getTokensFromToday() + allowed);
            currency.getModel().setLastUpdatedDate(LocalDate.now());
            currency.save();

            return (Map<Currency, Double>) (Map<?, ?>) Map.of(currency, allowed);
        });
    }

    public static CompletableFuture<CurrencyModel> getModelAsync(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Optional<CurrencyModel> optionalModel = StormDatabase.getInstance().getStorm()
                        .buildQuery(CurrencyModel.class)
                        .where("player_uuid", Where.EQUAL, player.getUniqueId())
                        .limit(1)
                        .execute()
                        .join()
                        .stream()
                        .findFirst();

                if (optionalModel.isPresent())
                    return optionalModel.get();

                CurrencyModel newModel = new CurrencyModel();
                newModel.setPlayerUuid(player.getUniqueId());
                newModel.setGrindTokens(0.0);
                newModel.setTokensFromToday(0.0);
                newModel.setCashFromToday(0.0);
                newModel.setLastUpdatedDate(LocalDate.now());

                StormDatabase.getInstance().saveStormModel(newModel);
                return newModel;
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het ophalen van jouw player data!"))
                );
                return null;
            }
        });
    }

    private static void checkIfNeedReset(Player player) {
        getModelAsync(player).thenAccept(model -> {
            GrindingCurrency currency = new GrindingCurrency(player.getUniqueId(), model);
            currency.checkIfNeedsReset();
        });
    }
}
