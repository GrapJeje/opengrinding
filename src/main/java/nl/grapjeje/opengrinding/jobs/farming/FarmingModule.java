package nl.grapjeje.opengrinding.jobs.farming;

import lombok.Getter;
import nl.grapjeje.core.SkullUtil;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.api.GrindingRegion;
import nl.grapjeje.opengrinding.api.Jobs;
import nl.grapjeje.opengrinding.api.Plant;
import nl.grapjeje.opengrinding.core.objects.CraftGrindingRegion;
import nl.grapjeje.opengrinding.jobs.farming.configuration.FarmingJobConfiguration;
import nl.grapjeje.opengrinding.jobs.farming.listeners.FarmingListener;
import nl.grapjeje.opengrinding.jobs.farming.listeners.TrampleListener;
import nl.grapjeje.opengrinding.models.GrindingRegionModel;
import nl.grapjeje.opengrinding.utils.JobModule;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FarmingModule extends JobModule {
    @Getter
    private static final List<Plant> plants = new CopyOnWriteArrayList<>();
    @Getter
    private static final List<GrindingRegion> farmingRegions = new ArrayList<>();
    @Getter
    private final static FarmingJobConfiguration config = new FarmingJobConfiguration(OpenGrinding.getInstance().getDataFolder());

    public FarmingModule() {
        super("farming");
    }

    @Override
    protected void onEnable() {
        OpenGrinding.getFramework().registerConfig(config);

        OpenGrinding.getFramework().registerListener(FarmingListener::new);
        OpenGrinding.getFramework().registerListener(TrampleListener::new);

        this.getAllFarmingRegions();
    }

    @Override
    protected void onDisable() {

    }

    public static ItemStack getBlockHead(nl.grapjeje.opengrinding.jobs.farming.objects.Plant plant) {
        ItemStack head = SkullUtil.getCustomHead(plant.getLink(), plant.getUuid());
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtil.filterMessage(plant.getItemName()));
            head.setItemMeta(meta);
        }
        return head;
    }

    private void getAllFarmingRegions() {
        farmingRegions.clear();
        try {
            Collection<GrindingRegionModel> allRegions = StormDatabase.getInstance().getStorm()
                    .buildQuery(GrindingRegionModel.class)
                    .execute()
                    .join();

            for (GrindingRegionModel model : allRegions) {
                GrindingRegion region = CraftGrindingRegion.get(model);
                if (region.getJobs().contains(Jobs.FARMING))
                    farmingRegions.add(region);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public JobConfig getJobConfig() {
        return config;
    }
}
