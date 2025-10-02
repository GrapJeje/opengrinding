package nl.grapjeje.opengrinding.jobs.core;

import lombok.Getter;
import nl.grapjeje.core.modules.Module;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.core.commands.GrindingRegionCommand;
import nl.grapjeje.opengrinding.jobs.core.commands.OpenGrindingCommand;
import nl.grapjeje.opengrinding.jobs.core.commands.SellCommand;
import nl.grapjeje.opengrinding.jobs.core.configuration.GrindingLevelsConfiguration;
import nl.grapjeje.opengrinding.jobs.core.configuration.GrindingShopConfiguration;
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
    private static GrindingLevelsConfiguration grindingLevelsConfiguration;
    @Getter
    private static GrindingShopConfiguration grindingShopConfiguration;

    @Getter
    private static final Map<UUID, PlayerGrindingModel> playerCache = new HashMap<>();

    public CoreModule() {
        super("core", true);
    }

    @Override
    protected void onEnable() {
        grindingLevelsConfiguration = new GrindingLevelsConfiguration(OpenGrinding.getInstance().getDataFolder());
        grindingShopConfiguration = new GrindingShopConfiguration(OpenGrinding.getInstance().getDataFolder());

        OpenGrinding.getFramework().registerCommand(OpenGrindingCommand::new);
        OpenGrinding.getFramework().registerCommand(GrindingRegionCommand::new);
        OpenGrinding.getFramework().registerCommand(SellCommand::new);

        OpenGrinding.getFramework().registerListener(PlayerRegionWandListener::new);

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
