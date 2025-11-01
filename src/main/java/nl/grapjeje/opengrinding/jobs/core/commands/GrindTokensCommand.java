package nl.grapjeje.opengrinding.jobs.core.commands;

import nl.grapjeje.core.command.Command;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.api.GrindingCurrency;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.core.objects.CraftGrindingCurrency;
import nl.grapjeje.opengrinding.utils.currency.CurrencyUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicReference;

public class GrindTokensCommand implements Command {
    @Override
    public String getName() {
        return "grindtokens";
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        Player player = source.getPlayer();
        if (player == null) {
            source.getSender().sendRichMessage("<warning>⚠ Alleen spelers kunnen dit commando gebruiken!");
            return;
        }
        if (!(CoreModule.getConfig().isSellInTokens() || CoreModule.getConfig().isBuyInTokens())) {
            player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Grindtokens staan momenteel uit!"));
            return;
        }

        AtomicReference<Double> playerTokens = new AtomicReference<>(0.0);
        AtomicReference<Double> tokensLeft = new AtomicReference<>(0.0);

        Bukkit.getScheduler().runTaskAsynchronously(OpenGrinding.getInstance(), () ->
                CurrencyUtil.getModelAsync(player).thenApply(model -> {
            GrindingCurrency currency = CraftGrindingCurrency.get(player.getUniqueId(), model);
            playerTokens.set(currency.getGrindTokens());
            double currentDayTokens = currency.getTokensFromToday();
            double maxTokensPerDay = CoreModule.getConfig().getTokenLimit();
            tokensLeft.set(maxTokensPerDay - currentDayTokens);

            String message = "<gold>Je hebt in totaal <yellow>" + playerTokens.get() + " <gold>grindtokens.";
            if (CoreModule.getConfig().isDailyLimit()) message += " Je kan er vandaag nog <yellow>" + tokensLeft + " <gold>halen!";
            String finalMessage = message;
            Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                    player.sendMessage(MessageUtil.filterMessage(finalMessage))
            );
            return null;
        }));
    }
}
