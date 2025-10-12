package nl.grapjeje.opengrinding.models;

import com.craftmend.storm.api.StormModel;
import com.craftmend.storm.api.markers.Column;
import com.craftmend.storm.api.markers.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "player_currency")
public class CurrencyModel extends StormModel {

    @Column(name = "player_uuid")
    private UUID playerUuid;

    @Column(name = "grind_tokens", defaultValue = "0")
    private Double grindTokens = 0.0;

    @Column(name = "tokens_from_today", defaultValue = "0")
    private Double tokensFromToday = 0.0;

    @Column(name = "cash_from_today", defaultValue = "0")
    private Double cashFromToday = 0.0;

    @Column(name = "last_updated", defaultValue = "0")
    private Integer lastUpdated = (int) LocalDate.now().toEpochDay();

    public LocalDate getLastUpdatedDate() {
        return LocalDate.ofEpochDay(lastUpdated);
    }

    public void setLastUpdatedDate(LocalDate date) {
        this.lastUpdated = (int) date.toEpochDay();
    }

    public boolean reachedTokenLimit() {
        if (CoreModule.getConfig().isDailyLimit()) {
            double limit = CoreModule.getConfig().getTokenLimit();
            return tokensFromToday >= limit;
        } else return false;
    }

    public boolean reachedCashLimit() {
        if (CoreModule.getConfig().isDailyLimit()) {
            double limit = CoreModule.getConfig().getTokenLimit();
            return cashFromToday >= limit;
        } else return false;
    }

    public boolean needsUpdate(LocalDate now) {
        long daysBetween = ChronoUnit.DAYS.between(getLastUpdatedDate(), now);
        return daysBetween >= 1;
    }
}
