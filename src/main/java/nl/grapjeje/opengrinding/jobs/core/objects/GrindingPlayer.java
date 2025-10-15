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

    public CompletableFuture<Void> save() {
        CoreModule.getPlayerCache().put(model.getPlayerUuid(), model);
        return CompletableFuture.runAsync(() ->
                StormDatabase.getInstance().saveStormModel(model)
        );
    }

    public static PlayerGrindingModel loadOrCreatePlayerModel(Player player, Jobs job) {
        if (CoreModule.getPlayerCache().containsKey(player.getUniqueId()))
            return CoreModule.getPlayerCache().get(player.getUniqueId());

        Optional<PlayerGrindingModel> playerModelOpt;
        try {
            playerModelOpt = StormDatabase.getInstance().getStorm()
                    .buildQuery(PlayerGrindingModel.class)
                    .where("player_uuid", Where.EQUAL , player.getUniqueId())
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
            throw new RuntimeException(ex);
        }

        return playerModelOpt.orElseGet(() -> {
            PlayerGrindingModel m = new PlayerGrindingModel();
            m.setPlayerUuid(player.getUniqueId());
            m.setJob(job);
            m.setLevel(0);
            m.setValue(0.0);
            GrindingPlayer gp = new GrindingPlayer(player.getUniqueId(), m);
            gp.save();
            Bukkit.getLogger().info("New player grind model made for " + player.getName());
            return m;
        });
    }

    /* ---------- Progression ---------- */
    public void addProgress(Jobs job, double xp) {
        LevelConfig config;
        switch (job) {
            case MINING -> config = MiningModule.getConfig();
            case LUMBER -> config = LumberModule.getConfig();
            default -> throw new IllegalStateException("Unknown job: " + job);
        }
        double oldXp = model.getValue();
        int oldLevel = model.getLevel();
        double newXp = oldXp + xp;

        double finalNewXp = newXp;
        Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                new PlayerValueChangeEvent(this, job, oldXp, finalNewXp).callEvent()
        );
        int currentLevel = oldLevel;
        while (currentLevel < config.getMaxLevel()) {
            double xpNeeded = config.getLevelOverride(currentLevel + 1) != null
                    ? config.getLevelOverride(currentLevel + 1)
                    : config.getXpForLevel(currentLevel + 1);

            if (newXp >= xpNeeded) {
                newXp -= xpNeeded;
                currentLevel++;

                int finalOldLevel = oldLevel;
                int finalCurrentLevel = currentLevel;
                Bukkit.getScheduler().runTask(OpenGrinding.getInstance(), () ->
                        new PlayerLevelChangeEvent(this, job, finalOldLevel, finalCurrentLevel).callEvent()
                );
                oldLevel = currentLevel;
            } else break;
        }
        model.setLevel(currentLevel);
        model.setValue(newXp);
    }
}
