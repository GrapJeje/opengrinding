package nl.grapjeje.opengrinding.api;

import nl.openminetopia.api.player.objects.MinetopiaPlayer;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public interface GrindingCurrency {

    MinetopiaPlayer getPlayer();

    CompletableFuture<Void> save();

    Double getGrindTokens();
    void setGrindTokens(Double grindTokens);

    Double getCashFromToday();
    void setCashFromToday(Double cash);

    Double getTokensFromToday();
    void setTokensFromToday(Double grindTokens);

    boolean reachedTokenLimit();
    boolean reachedCashLimit();

    boolean checkIfNeedsReset();

    LocalDate getLastUpdatedDate();
    void setLastUpdatedDate(LocalDate date);

    boolean needsUpdate(LocalDate now);
}
