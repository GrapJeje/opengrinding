package nl.grapjeje.opengrinding.jobs.farming.plants;

import lombok.AccessLevel;
import lombok.Getter;
import nl.grapjeje.opengrinding.api.Plant;
import nl.grapjeje.opengrinding.jobs.farming.growth.GrowthStage;

@Getter(AccessLevel.PROTECTED)
public abstract class GrowablePlant implements Plant {
    private GrowthStage stage;
    private final int maxStage;
    private final long growTimeMs;

    protected GrowablePlant(int maxStage, long growTimeMs) {
        this.stage = GrowthStage.SEED;
        this.maxStage = maxStage;
        this.growTimeMs = growTimeMs;
    }

    @Override
    public void grow() {
        if (!stage.isMax()) {
            stage = stage.next();
            if ((stage.getId() + 1) == maxStage)
                stage = GrowthStage.READY;
        }
    }

    @Override
    public boolean canHarvest() {
        return stage.isMax();
    }

    public String getStringId() {
        return this.getId().toString();
    }
}
