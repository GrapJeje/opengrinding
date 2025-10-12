package nl.grapjeje.opengrinding.utils;

import nl.grapjeje.core.modules.Module;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;

public abstract class JobModule extends Module {
    public JobModule(String name) {
        super(name);
    }

    public JobModule(String name, boolean alwaysEnabled) {
        super(name, alwaysEnabled);
    }

    public abstract JobConfig getJobConfig();
}
