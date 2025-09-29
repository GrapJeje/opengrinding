package nl.grapjeje.opengrinding.jobs.core.objects;

import lombok.Getter;
import net.objecthunter.exp4j.ExpressionBuilder;
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
    private final PlayerGrindingModel playerGrindingModel;

    public GrindingPlayer(UUID uuid, PlayerGrindingModel playerGrindingModel) {
        this.player = PlayerManager.getInstance().getOnlineMinetopiaPlayer(Bukkit.getPlayer(uuid));
        this.playerGrindingModel = playerGrindingModel;
    }

    public CompletableFuture<Void> save() {
        return CompletableFuture.runAsync(() -> StormDatabase.getInstance().saveStormModel(playerGrindingModel));
    }

    /* ---------- Progression ---------- */

    public int getLevel() {
        return playerGrindingModel.getLevel();
    }

    public double getXp() {
        return playerGrindingModel.getValue();
    }

    public void setProgress(Jobs job, int level, double xp) {
        int oldLevel = playerGrindingModel.getLevel();
        double oldXp = playerGrindingModel.getValue();

        if (oldXp != xp) new PlayerValueChangeEvent(this, job, oldXp, xp).callEvent();
        if (oldLevel != level) new PlayerLevelChangeEvent(this, job, oldLevel, level).callEvent();

        playerGrindingModel.setLevel(level);
        playerGrindingModel.setValue(xp);
        this.save();
    }

    public void addProgress(Jobs job, double xp) {
        double oldXp = playerGrindingModel.getValue();
        int oldLevel = playerGrindingModel.getLevel();
        double newXp = oldXp + xp;

        new PlayerValueChangeEvent(this, job, oldXp, newXp).callEvent();

        GrindingLevelsConfiguration config = CoreModule.getGrindingLevelsConfiguration();

        int currentLevel = oldLevel;
        while (currentLevel < config.getMaxLevel()) {
            int xpNeeded = getXpForLevel(currentLevel, config);
            if (newXp >= xpNeeded) {
                newXp -= xpNeeded;
                currentLevel++;
                new PlayerLevelChangeEvent(this, job, oldLevel, currentLevel).callEvent();
                oldLevel = currentLevel;
            } else break;
        }

        playerGrindingModel.setLevel(currentLevel);
        playerGrindingModel.setValue(newXp);
        this.save();
    }

    private int getXpForLevel(int level, GrindingLevelsConfiguration config) {
        if (config.getOverrides().containsKey(level))
            return config.getOverrides().get(level);
        double result = new ExpressionBuilder(config.getFormula())
                .variable("level")
                .build()
                .setVariable("level", level)
                .evaluate();
        return (int) Math.max(0, result);
    }
}
