package nl.grapjeje.opengrinding.jobs.farming.growth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GrowthStage {
    SEED(0),
    SPROUT(1),
    SMALL(2),
    MEDIUM(3),
    LARGE(4),
    FLOWERING(5),
    READY(6);

    final Integer id;

    public boolean isMax() {
        return this == READY;
    }

    public GrowthStage next() {
        return values()[ordinal() + 1];
    }
}