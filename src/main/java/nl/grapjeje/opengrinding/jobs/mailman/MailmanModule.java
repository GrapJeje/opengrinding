package nl.grapjeje.opengrinding.jobs.mailman;

import lombok.Getter;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.mailman.commands.MailmanCommand;
import nl.grapjeje.opengrinding.jobs.mailman.commands.PackageCommand;
import nl.grapjeje.opengrinding.jobs.mailman.configuration.MailmanJobConfiguration;
import nl.grapjeje.opengrinding.jobs.mailman.listeners.*;
import nl.grapjeje.opengrinding.jobs.mailman.objects.MailmanJob;
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
        OpenGrinding.getFramework().registerCommand(PackageCommand::new);

        OpenGrinding.getFramework().registerListener(InteractionListener::new);
        OpenGrinding.getFramework().registerListener(ItemListener::new);
        OpenGrinding.getFramework().registerListener(PlayerLeaveListener::new);
        OpenGrinding.getFramework().registerListener(DeliverPackageListener::new);
    }

    @Override
    protected void onDisable() {
        MailmanJob.getJobs().values().forEach(job ->
                job.stop(false));
    }
}
