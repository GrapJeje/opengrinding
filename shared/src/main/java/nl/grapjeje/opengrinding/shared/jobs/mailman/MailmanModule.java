package nl.grapjeje.opengrinding.shared.jobs.mailman;

import lombok.Getter;
import nl.grapjeje.opengrinding.shared.OpenGrinding;
import nl.grapjeje.opengrinding.shared.jobs.mailman.commands.MailmanCommand;
import nl.grapjeje.opengrinding.shared.jobs.mailman.commands.PackageCommand;
import nl.grapjeje.opengrinding.shared.jobs.mailman.configuration.MailmanJobConfiguration;
import nl.grapjeje.opengrinding.shared.jobs.mailman.listeners.DeliverPackageListener;
import nl.grapjeje.opengrinding.shared.jobs.mailman.listeners.InteractionListener;
import nl.grapjeje.opengrinding.shared.jobs.mailman.listeners.ItemListener;
import nl.grapjeje.opengrinding.shared.jobs.mailman.listeners.PlayerLeaveListener;
import nl.grapjeje.opengrinding.shared.jobs.mailman.objects.MailmanJob;
import nl.grapjeje.opengrinding.shared.utils.JobModule;
import nl.grapjeje.opengrinding.shared.utils.configuration.JobConfig;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MailmanModule extends JobModule {
    @Getter
    private static MailmanJobConfiguration config = new MailmanJobConfiguration(OpenGrinding.getInstance().getDataFolder());
    @Getter
    private static String packageUrl = "https://textures.minecraft.net/texture/37c648e832d5ecc7a0ca94dcf4308a02714a052a76e594be5730a713bc41a3dd";
    @Getter
    private static Map<UUID, Long> playerCooldown = new ConcurrentHashMap<>();

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
