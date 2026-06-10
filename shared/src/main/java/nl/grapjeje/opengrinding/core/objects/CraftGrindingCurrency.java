package nl.grapjeje.opengrinding.core.objects;

import lombok.Getter;
import nl.grapjeje.core.registry.AutoRegistry;
import nl.grapjeje.core.registry.Registry;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.api.GrindingCurrency;
import nl.grapjeje.opengrinding.core.CoreModule;
import nl.grapjeje.opengrinding.models.CurrencyModel;
import nl.openminetopia.api.player.PlayerManager;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AutoRegistry
@Getter
public class CraftGrindingCurrency implements GrindingCurrency {
    private final MinetopiaPlayer player;
    private final CurrencyModel model;

    public CraftGrindingCurrency(UUID uuid, CurrencyModel grindTokensModel) {
        this.player = PlayerManager.getInstance().getOnlineMinetopiaPlayer(Bukkit.getPlayer(uuid));
        this.model = grindTokensModel;
    }

    public static GrindingCurrency get(UUID uuid, CurrencyModel model) {
        return Registry.get(
                GrindingCurrency.class,
                uuid.toString(),
                (args) -> new CraftGrindingCurrency((UUID) args[0], (CurrencyModel) args[1]),
                uuid, model
        );
    }

    @Override
    public CompletableFuture<Void> save() {
        return CompletableFuture.runAsync(() -> StormDatabase.getInstance().saveStormModel(model));
    }

    @Override
    public Double getGrindTokens() {
        return model.getGrindTokens();
    }

    @Override
    public void setGrindTokens(Double grindTokens) {
        model.setGrindTokens(grindTokens);
    }

    @Override
    public Double getCashFromToday() {
        return model.getCashFromToday();
    }

    @Override
    public void setCashFromToday(Double cash) {
        model.setCashFromToday(cash);
    }

    @Override
    public Double getTokensFromToday() {
        return model.getTokensFromToday();
    }

    @Override
    public void setTokensFromToday(Double grindTokens) {
        model.setTokensFromToday(grindTokens);
    }

    @Override
    public boolean reachedCashLimit() {
        return model.reachedCashLimit();
    }

    @Override
    public boolean reachedTokenLimit() {
        return model.reachedTokenLimit();
    }

    @Override
    public boolean checkIfNeedsReset() {
        LocalDate now = LocalDate.now();
        if (model.needsUpdate(now)) {
            model.setTokensFromToday(0.0);
            model.setCashFromToday(0.0);
            model.setLastUpdatedDate(now);
            this.save();

            Player player = this.getPlayer().getBukkit().getPlayer();
            if (player == null) return false;
            if (!CoreModule.getConfig().isDailyLimit()) return false;

            if (CoreModule.getConfig().isSellInTokens())
                player.sendMessage(MessageUtil.filterMessage("<green>Jouw grindtokens limiet is gereset!"));
            else player.sendMessage(MessageUtil.filterMessage("<green>Jouw grinding cash limiet is gereset!"));
            return true;
        } else return false;
    }

    @Override
    public LocalDate getLastUpdatedDate() {
        return model.getLastUpdatedDate();
    }

    @Override
    public void setLastUpdatedDate(LocalDate date) {
        model.setLastUpdatedDate(date);
    }

    @Override
    public boolean needsUpdate(LocalDate now) {
        return model.needsUpdate(now);
    }
}
