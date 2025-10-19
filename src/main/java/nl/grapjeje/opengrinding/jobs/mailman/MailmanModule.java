package nl.grapjeje.opengrinding.jobs.mailman;

import lombok.Getter;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.mailman.configuration.MailmanJobConfiguration;
import nl.grapjeje.opengrinding.jobs.mining.configuration.MiningJobConfiguration;
import nl.grapjeje.opengrinding.utils.JobModule;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;

public class MailmanModule extends JobModule {
    @Getter
    private static MailmanJobConfiguration config = new MailmanJobConfiguration(OpenGrinding.getInstance().getDataFolder());


    public MailmanModule() {
        super("mailman");
    }

    @Override
    public JobConfig getJobConfig() {
        return config;
    }

    @Override
    protected void onEnable() {
        OpenGrinding.getFramework().registerConfig(config);
    }

    @Override
    protected void onDisable() {

    }
}
