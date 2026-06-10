package nl.grapjeje.opengrinding.models;

import com.craftmend.storm.api.StormModel;
import com.craftmend.storm.api.enums.Where;
import com.craftmend.storm.api.markers.Column;
import com.craftmend.storm.api.markers.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.grapjeje.opengrinding.api.Jobs;
import nl.grapjeje.opengrinding.core.CoreModule;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "player_grinding")
public class PlayerGrindingModel extends StormModel {

    @Column(name = "player_uuid")
    private UUID playerUuid;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "level", defaultValue = "0")
    private Integer level = 0;

    @Column(name = "value", defaultValue = "0")
    private Double value = 0.0;

    public Jobs getJob() {
        if (jobName == null) return null;
        return Jobs.valueOf(jobName);
    }

    public void setJob(Jobs job) {
        this.jobName = job.name();
    }

    private static final Map<UUID, Object> playerLocks = new ConcurrentHashMap<>();

    public static PlayerGrindingModel createNew(Player player, Jobs job) {
        Object lock = playerLocks.computeIfAbsent(player.getUniqueId(), k -> new Object());
        try {
            synchronized (lock) {
                Optional<PlayerGrindingModel> existing = StormDatabase.getInstance().getStorm()
                        .buildQuery(PlayerGrindingModel.class)
                        .where("player_uuid", Where.EQUAL, player.getUniqueId())
                        .where("job_name", Where.EQUAL, job.name())
                        .limit(1)
                        .execute()
                        .join()
                        .stream()
                        .findFirst();
                if (existing.isPresent()) return existing.get();

                PlayerGrindingModel m = new PlayerGrindingModel();
                m.setPlayerUuid(player.getUniqueId());
                m.setJob(job);
                m.setLevel(0);
                m.setValue(0.0);

                StormDatabase.getInstance().getStorm().save(m);
                CoreModule.putCachedModel(player.getUniqueId(), job, m);
                return m;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
