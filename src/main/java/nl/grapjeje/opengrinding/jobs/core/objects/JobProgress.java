package nl.grapjeje.opengrinding.jobs.core.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobProgress {
    private int level;
    private double xp;

    public JobProgress(int level, double xp) {
        this.level = level;
        this.xp = xp;
    }
}
