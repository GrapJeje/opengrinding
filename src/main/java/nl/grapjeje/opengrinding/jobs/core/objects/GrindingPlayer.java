package nl.grapjeje.opengrinding.jobs.core.objects;

import com.craftmend.storm.api.enums.Where;
import lombok.Getter;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.Jobs;
import nl.grapjeje.opengrinding.jobs.core.CoreModule;
import nl.grapjeje.opengrinding.jobs.core.events.PlayerLevelChangeEvent;
import nl.grapjeje.opengrinding.jobs.core.events.PlayerValueChangeEvent;
import nl.grapjeje.opengrinding.jobs.lumber.LumberModule;
import nl.grapjeje.opengrinding.jobs.mailman.MailmanModule;
import nl.grapjeje.opengrinding.jobs.mining.MiningModule;
import nl.grapjeje.opengrinding.models.PlayerGrindingModel;
import nl.grapjeje.opengrinding.utils.configuration.LevelConfig;
import nl.openminetopia.api.player.PlayerManager;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
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

    public CompletableFuture<Void> save(Jobs job) {
        CoreModule.putCachedModel(model.getPlayerUuid(), job, model);
        return CompletableFuture.runAsync(() ->
                StormDatabase.getInstance().saveStormModel(model));
    }

    public static CompletableFuture<PlayerGrindingModel> loadOrCreatePlayerModelAsync(Player player, Jobs job) {
        PlayerGrindingModel cached = CoreModule.getCachedModel(player.getUniqueId(), job);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        return CompletableFuture.supplyAsync(() -> {
            Optional<PlayerGrindingModel> playerModelOpt;
            try {
                playerModelOpt = StormDatabase.getInstance().getStorm()
                        .buildQuery(PlayerGrindingModel.class)
                        .where("player_uuid", Where.EQUAL, player.getUniqueId())
                        .where("job_name", Where.EQUAL, job.name())
                        .limit(1)
                        .execute()
                        .join()
                        .stream()
                        .findFirst();
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        player.sendMessage(MessageUtil.filterMessage("<warning>⚠ Er is een fout opgetreden bij het ophalen van jouw spelersdata!"))
                );
                return PlayerGrindingModel.createNew(player, job);
            }

            PlayerGrindingModel model = playerModelOpt.orElseGet(() -> {
                OpenGrinding.getInstance().getLogger().info("New player grind model made for " + player.getName() + " (" + job.name() + ")");
                return PlayerGrindingModel.createNew(player, job);
            });

            CoreModule.putCachedModel(player.getUniqueId(), job, model);
            return model;
        });
    }

    /* ---------- Progression ---------- */
    public void addProgress(Jobs job, double xp) {
        LevelConfig config;
        switch (job) {
            case MINING -> config = MiningModule.getConfig();
            case LUMBER -> config = LumberModule.getConfig();
            case MAILMAN -> config = MailmanModule.getConfig();
            default -> throw new IllegalStateException("Unknown job: " + job);
        }

        double oldXp = model.getValue();
        int oldLevel = model.getLevel();
        double newXp = oldXp + xp;

        double finalNewXp = newXp;
        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                new PlayerValueChangeEvent(this, job, oldXp, finalNewXp).callEvent());

        int currentLevel = oldLevel;
        while (currentLevel < config.getMaxLevel()) {
            Double levelOverride = config.getLevelOverride(currentLevel);
            double configXp = config.getXpForLevel(currentLevel);
            Bukkit.getLogger().severe("[DEBUG] Level: " + currentLevel +
                    ", Override: " + levelOverride +
                    ", Config XP: " + configXp);

            double xpNeeded = (levelOverride != null && levelOverride > 0.0)
                    ? levelOverride
                    : configXp;

            Bukkit.getLogger().severe("[LevelSystem] Level: " + currentLevel + ", Needed XP: " + xpNeeded +
                    ((levelOverride != null && levelOverride > 0.0) ? " (Overridden)" : ""));

            if (xpNeeded <= 0) break;

            if (newXp >= xpNeeded) {
                newXp -= xpNeeded;
                currentLevel++;

                int finalOldLevel = oldLevel;
                int finalCurrentLevel = currentLevel;
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        new PlayerLevelChangeEvent(this, job, finalOldLevel, finalCurrentLevel).callEvent());

                oldLevel = currentLevel;
            } else break;
        }

        model.setLevel(currentLevel);
        model.setValue(newXp);

        this.save(job).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
}
