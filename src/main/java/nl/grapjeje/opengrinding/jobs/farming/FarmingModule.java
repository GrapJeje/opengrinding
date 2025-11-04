package nl.grapjeje.opengrinding.jobs.farming;

import lombok.Getter;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.farming.configuration.FarmingJobConfiguration;
import nl.grapjeje.opengrinding.jobs.farming.listeners.FarmingListener;
import nl.grapjeje.opengrinding.jobs.fishing.configuration.FishingJobConfiguration;
import nl.grapjeje.opengrinding.utils.JobModule;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;

public class FarmingModule extends JobModule {
    @Getter
    private final static FarmingJobConfiguration config = new FarmingJobConfiguration(OpenGrinding.getInstance().getDataFolder());

    public FarmingModule() {
        super("farming");
    }

    @Override
    protected void onEnable() {
        OpenGrinding.getFramework().registerConfig(config);

        OpenGrinding.getFramework().registerListener(FarmingListener::new);
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public JobConfig getJobConfig() {
        return config;
    }
}
