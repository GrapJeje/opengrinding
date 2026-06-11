package nl.grapjeje.opengrinding.shared.jobs.fishing;

import lombok.Getter;
import nl.grapjeje.opengrinding.shared.OpenGrinding;
import nl.grapjeje.opengrinding.shared.jobs.fishing.commands.FishLootTableCommand;
import nl.grapjeje.opengrinding.shared.jobs.fishing.listeners.PlayerSessionListener;
import nl.grapjeje.opengrinding.shared.jobs.fishing.configuration.FishingJobConfiguration;
import nl.grapjeje.opengrinding.shared.jobs.fishing.games.FishingGame;
import nl.grapjeje.opengrinding.shared.jobs.fishing.listeners.PlayerCatchListener;
import nl.grapjeje.opengrinding.shared.utils.JobModule;
import nl.grapjeje.opengrinding.shared.utils.configuration.JobConfig;

public class FishingModule extends JobModule {
    @Getter
    private final static FishingJobConfiguration config = new FishingJobConfiguration(OpenGrinding.getInstance().getDataFolder());

    public FishingModule() {
        super("fishing");
    }

    @Override
    protected void onEnable() {
        OpenGrinding.getFramework().registerConfig(config);

        OpenGrinding.getFramework().registerCommand(FishLootTableCommand::new);

        OpenGrinding.getFramework().registerListener(PlayerCatchListener::new);
        OpenGrinding.getFramework().registerListener(PlayerSessionListener::new);
    }

    @Override
    protected void onDisable() {
        FishingGame.getPlayersInGame().values().forEach(
                game -> game.stop(false));
    }

    @Override
    public boolean isDisabled() {
        if (!getConfig().isEnabled())
            this.setDisabled();
        return super.isDisabled();
    }

    @Override
    public JobConfig getJobConfig() {
        return config;
    }
}
