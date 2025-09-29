package nl.grapjeje.opengrinding.models;

import com.craftmend.storm.api.StormModel;
import com.craftmend.storm.api.markers.Column;
import com.craftmend.storm.api.markers.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.grapjeje.opengrinding.jobs.Jobs;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "player_grinding")
public class PlayerGrindingModel extends StormModel {

    @Column(name = "player_uuid")
    private UUID playerUuid;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "level", defaultValue = "1")
    private Integer level;

    @Column(name = "value", defaultValue = "0")
    private Double value;

    public Jobs getJob() {
        return Jobs.valueOf(jobName);
    }

    public void setJob(Jobs job) {
        this.jobName = job.name();
    }
}
