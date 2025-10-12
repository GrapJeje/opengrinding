package nl.grapjeje.opengrinding.jobs.fishing;

import lombok.Getter;
import nl.grapjeje.core.modules.Module;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.fishing.commands.FishLootTableCommand;
import nl.grapjeje.opengrinding.jobs.fishing.listeners.PlayerSessionListener;
import nl.grapjeje.opengrinding.jobs.fishing.configuration.FishingJobConfiguration;
import nl.grapjeje.opengrinding.jobs.fishing.games.FishingGame;
import nl.grapjeje.opengrinding.jobs.fishing.listeners.PlayerCatchListener;
import nl.grapjeje.opengrinding.utils.JobModule;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
