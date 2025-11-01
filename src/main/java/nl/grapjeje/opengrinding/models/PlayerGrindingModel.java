package nl.grapjeje.opengrinding.models;

import com.craftmend.storm.api.StormModel;
import com.craftmend.storm.api.markers.Column;
import com.craftmend.storm.api.markers.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.grapjeje.opengrinding.api.Jobs;
import org.bukkit.entity.Player;

import java.util.UUID;

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

    public static PlayerGrindingModel createNew(Player player, Jobs job) {
        PlayerGrindingModel m = new PlayerGrindingModel();
        m.setPlayerUuid(player.getUniqueId());
        m.setJob(job);
        m.setLevel(0);
        m.setValue(0.0);
        return m;
    }
}
