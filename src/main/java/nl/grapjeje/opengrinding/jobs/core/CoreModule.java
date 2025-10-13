package nl.grapjeje.opengrinding.jobs.core;

import lombok.Getter;
import nl.grapjeje.core.modules.Module;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.core.commands.*;
import nl.grapjeje.opengrinding.jobs.core.configuration.DefaultConfiguration;
import nl.grapjeje.opengrinding.jobs.core.listeners.HeadBlockerListener;
import nl.grapjeje.opengrinding.jobs.core.listeners.PlayerJoinListener;
import nl.grapjeje.opengrinding.jobs.core.listeners.PlayerLevelUpListener;
import nl.grapjeje.opengrinding.jobs.core.listeners.PlayerRegionWandListener;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CoreModule extends Module {
    @Getter
    private static final Map<UUID, PlayerGrindingModel> playerCache = new HashMap<>();
    @Getter
    private static final DefaultConfiguration config = new DefaultConfiguration(OpenGrinding.getInstance().getDataFolder());

    public CoreModule() {
        super("core", true);
    }

    @Override
    protected void onEnable() {
        OpenGrinding.getFramework().registerConfig(config);

        OpenGrinding.getFramework().registerCommand(OpenGrindingCommand::new);
        OpenGrinding.getFramework().registerCommand(GrindingRegionCommand::new);
        OpenGrinding.getFramework().registerCommand(SellCommand::new);
        OpenGrinding.getFramework().registerCommand(ShopCommand::new);
        OpenGrinding.getFramework().registerCommand(FixSkullCommand::new);
        OpenGrinding.getFramework().registerCommand(GrindTokensCommand::new);
        OpenGrinding.getFramework().registerCommand(DailyLimitCommand::new);

        OpenGrinding.getFramework().registerListener(PlayerRegionWandListener::new);
        OpenGrinding.getFramework().registerListener(HeadBlockerListener::new);
        OpenGrinding.getFramework().registerListener(PlayerLevelUpListener::new);
        OpenGrinding.getFramework().registerListener(PlayerJoinListener::new);

        try {
            Bukkit.getLogger().info("Loading grinding regions...");
            List<GrindingRegionModel> models = StormDatabase.getInstance().getStorm()
                    .buildQuery(GrindingRegionModel.class)
                    .execute()
                    .join()
                    .stream()
                    .toList();
            Bukkit.getLogger().info("Loaded " + models.size() + " grinding regions.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDisable() {

    }
}
