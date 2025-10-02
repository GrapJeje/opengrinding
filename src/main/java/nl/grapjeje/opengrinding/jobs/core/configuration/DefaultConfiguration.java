package nl.grapjeje.opengrinding.jobs.core.configuration;

import lombok.Getter;
import nl.grapjeje.core.Config;

import java.io.File;

@Getter
public class DefaultConfiguration extends Config {
    private final boolean jobSkullsOnPlayerhead;

    public DefaultConfiguration(File file) {
        super(file, "config.yml", "default/config.yml", true);

        this.jobSkullsOnPlayerhead = config.getBoolean("jobskullsonplayerhead", false);
    }
}
