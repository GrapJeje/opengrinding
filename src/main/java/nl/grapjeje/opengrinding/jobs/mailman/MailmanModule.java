package nl.grapjeje.opengrinding.jobs.mailman;

import lombok.Getter;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.mailman.commands.MailmanCommand;
import nl.grapjeje.opengrinding.jobs.mailman.configuration.MailmanJobConfiguration;
import nl.grapjeje.opengrinding.utils.JobModule;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;

public class MailmanModule extends JobModule {
    @Getter
    private static MailmanJobConfiguration config = new MailmanJobConfiguration(OpenGrinding.getInstance().getDataFolder());
    @Getter
    private static String packageUrl = "https://textures.minecraft.net/texture/37c648e832d5ecc7a0ca94dcf4308a02714a052a76e594be5730a713bc41a3dd";

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

        OpenGrinding.getFramework().registerCommand(MailmanCommand::new);
    }

    @Override
    protected void onDisable() {

    }
}
