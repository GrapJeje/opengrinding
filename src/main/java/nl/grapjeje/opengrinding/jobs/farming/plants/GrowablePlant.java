package nl.grapjeje.opengrinding.jobs.farming.plants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import nl.grapjeje.opengrinding.api.Plant;
import nl.grapjeje.opengrinding.jobs.farming.objects.GrowthStage;
import org.bukkit.block.Block;

@Getter(AccessLevel.PROTECTED)
public abstract class GrowablePlant implements Plant {
    @Setter(AccessLevel.PROTECTED)
    private GrowthStage stage;
    @Getter
    private final Block block;
    private final int maxStage;
    private final long growTimeMs;

    protected GrowablePlant(Block block, GrowthStage stage, int maxStage, long growTimeMs) {
        this.block = block;
        this.stage = stage;
        this.maxStage = maxStage;
        this.growTimeMs = growTimeMs;
    }

    protected GrowablePlant(Block block, int maxStage, long growTimeMs) {
        this(block, GrowthStage.SEED, maxStage, growTimeMs);
    }

    @Override
    public void grow() {
        if (stage.isMax()) return;
        stage = stage.next();
        if ((stage.getId() + 1) == maxStage)
            stage = GrowthStage.READY;
    }

    @Override
    public boolean canHarvest() {
        return stage.isMax();
    }

    public String getStringId() {
        return this.getId().toString();
    }

    protected Plant getPlant(GrowablePlant plant) {
        return plant != null ? (Plant) plant : null;
    }
}
