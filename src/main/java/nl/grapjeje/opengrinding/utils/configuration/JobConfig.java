package nl.grapjeje.opengrinding.utils.configuration;

import lombok.Getter;
import nl.grapjeje.core.Config;

import java.io.File;

@Getter
public abstract class JobConfig extends Config {
    protected boolean enabled;

    public JobConfig(File folder, String name, String defaultResource, boolean mergeDefaults) {
        super(folder, "jobs", name, defaultResource, mergeDefaults);
    }
}
