package nl.grapjeje.opengrinding.shared.core.listeners;

import nl.grapjeje.opengrinding.shared.OpenGrinding;
import nl.grapjeje.opengrinding.shared.api.GrindingCurrency;
import nl.grapjeje.opengrinding.shared.core.objects.CraftGrindingCurrency;
import nl.grapjeje.opengrinding.shared.utils.currency.CurrencyUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(OpenGrinding.getInstance(), () -> {
            GrindingCurrency currency = CurrencyUtil.getModelAsync(player)
                    .thenApply(model -> CraftGrindingCurrency.get(player.getUniqueId(), model))
                    .join();
            if (currency == null) return;

            currency.checkIfNeedsReset();
        }, 50L);
    }
}
