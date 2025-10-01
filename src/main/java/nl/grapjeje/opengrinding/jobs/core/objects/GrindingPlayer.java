package nl.grapjeje.opengrinding.jobs.core.objects;

import lombok.Getter;
import net.objecthunter.exp4j.ExpressionBuilder;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.core.configuration.GrindingLevelsConfiguration;
import nl.grapjeje.opengrinding.jobs.core.events.PlayerLevelChangeEvent;
import nl.grapjeje.opengrinding.jobs.core.events.PlayerValueChangeEvent;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.openminetopia.api.player.PlayerManager;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GrindingPlayer {
    @Getter
    private final MinetopiaPlayer player;
    private final PlayerGrindingModel model;

    public GrindingPlayer(UUID uuid, PlayerGrindingModel playerGrindingModel) {
        this.player = PlayerManager.getInstance().getOnlineMinetopiaPlayer(Bukkit.getPlayer(uuid));
        this.model = playerGrindingModel;
    }

    public CompletableFuture<Void> save() {
        CoreModule.getPlayerCache().put(model.getPlayerUuid(), model);
        return CompletableFuture.runAsync(() ->
                StormDatabase.getInstance().saveStormModel(model)
        );
    }

    /* ---------- Progression ---------- */
    public void addProgress(Jobs job, double xp) {
        GrindingLevelsConfiguration config = CoreModule.getGrindingLevelsConfiguration();

        double oldXp = model.getValue();
        int oldLevel = model.getLevel();
        double newXp = oldXp + xp;
        double finalNewXp = newXp;
        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
            new PlayerValueChangeEvent(this, job, oldXp, finalNewXp).callEvent();
        });

        int currentLevel = oldLevel;
        while (currentLevel < config.getMaxLevel()) {
            int xpNeeded = this.getXpForLevel(currentLevel + 1, config);
            if (newXp >= xpNeeded) {
                newXp -= xpNeeded;
                currentLevel++;
                int finalOldLevel = oldLevel;
                int finalCurrentLevel = currentLevel;
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () -> {
                    new PlayerLevelChangeEvent(this, job, finalOldLevel, finalCurrentLevel).callEvent();
                });
                oldLevel = currentLevel;
            } else break;
        }

        model.setLevel(currentLevel);
        model.setValue(newXp);
    }

    private int getXpForLevel(int level, GrindingLevelsConfiguration config) {
        Integer override = config.getOverrides().get(level);
        if (override != null && override > 0) return override;

        double result = new ExpressionBuilder(config.getFormula())
                .variable("level")
                .build()
                .setVariable("level", level)
                .evaluate();
        return (int) Math.max(0, result);
    }
}
