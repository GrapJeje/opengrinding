package nl.grapjeje.opengrinding.jobs.core.commands;

import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.core.objects.GrindingCurrency;
import nl.grapjeje.opengrinding.utils.currency.CurrencyUtil;
import nl.openminetopia.OpenMinetopia;
import nl.openminetopia.api.player.PlayerManager;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import nl.openminetopia.modules.banking.BankingModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicReference;

public class DailyLimitCommand implements Command {
    @Override
    public String getName() {
        return "dailylimit";
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage("Dit command kan alleen door een speler uitgevoerd worden.");
            return;
        }
        if (!CoreModule.getConfig().isDailyLimit()) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is momenteel geen dagelijks limiet!"));
            return;
        }

        CurrencyUtil.getModelAsync(player)
                .thenAccept(model -> {
                    GrindingCurrency currency = new GrindingCurrency(player.getUniqueId(), model);

                    StringBuilder sb = new StringBuilder();
                    sb.append("<gold>Je kan vandaag nog ");

                    if (CoreModule.getConfig().isSellInTokens()) {
                        double currentDayTokens = currency.getModel().getTokensFromToday();
                        double maxTokensPerDay = CoreModule.getConfig().getTokenLimit();
                        double tokensLeft = Math.max(0.0, maxTokensPerDay - currentDayTokens);

                        sb.append("<yellow>").append(tokensLeft).append(" tokens");
                    } else {
                        double currentDayCash = currency.getModel().getCashFromToday();
                        double maxCashPerDay = CoreModule.getConfig().getCashLimit();
                        double cashLeft = Math.max(0.0, maxCashPerDay - currentDayCash);

                        sb.append("<yellow>").append(cashLeft);
                    }

                    sb.append(" <gold>halen!");
                    String finalMessage = sb.toString();

                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                            player.sendMessage(MessageUtil.filterMessage(finalMessage))
                    );
                })
                .exceptionally(ex -> {
                    Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is iets fout gegaan bij het laden van je dagelijkse limiet."))
                    );
                    ex.printStackTrace();
                    return null;
                });
    }
}
