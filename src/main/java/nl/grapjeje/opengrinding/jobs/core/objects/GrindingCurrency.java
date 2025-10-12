package nl.grapjeje.opengrinding.jobs.core.objects;

import lombok.Getter;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.models.CurrencyModel;
import nl.openminetopia.api.player.PlayerManager;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
public class GrindingCurrency {
    private final MinetopiaPlayer player;
    private final CurrencyModel model;

    public GrindingCurrency(UUID uuid, CurrencyModel grindTokensModel) {
        this.player = PlayerManager.getInstance().getOnlineMinetopiaPlayer(Bukkit.getPlayer(uuid));
        this.model = grindTokensModel;
    }

    public CompletableFuture<Void> save() {
        return CompletableFuture.runAsync(() -> StormDatabase.getInstance().saveStormModel(model));
    }

    public boolean reachedCashLimit() {
        return model.reachedCashLimit();
    }

    public boolean reachedTokenLimit() {
        return model.reachedTokenLimit();
    }

    public boolean checkIfNeedsReset() {
        LocalDate now = LocalDate.now();
        if (model.needsUpdate(now)) {
            model.setTokensFromToday(0.0);
            model.setCashFromToday(0.0);
            model.setLastUpdatedDate(now);
            this.save();

            Player player = this.getPlayer().getBukkit().getPlayer();
            if (player == null) return true;

            if (CoreModule.getConfig().isSellInTokens())
                player.sendMessage(MessageUtil.filterMessage("<green>Jouw grindtokens limiet is gereset!"));
            else player.sendMessage(MessageUtil.filterMessage("<green>Jouw grinding cash limiet is gereset!"));
            return true;
        } else return false;
    }
}
