package nl.grapjeje.opengrinding.jobs.core.listeners;

import nl.grapjeje.opengrinding.jobs.core.objects.GrindingCurrency;
import nl.grapjeje.opengrinding.utils.currency.CurrencyUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        GrindingCurrency currency = CurrencyUtil.getModelAsync(player)
                .thenApply(model -> new GrindingCurrency(player.getUniqueId(), model))
                .join();
        if (currency == null) return;

        currency.checkIfNeedsReset();
    }
}
