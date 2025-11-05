package nl.grapjeje.opengrinding.jobs.farming;

import lombok.Getter;
import nl.grapjeje.core.SkullUtil;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.api.Plant;
import nl.grapjeje.opengrinding.jobs.farming.configuration.FarmingJobConfiguration;
import nl.grapjeje.opengrinding.jobs.farming.listeners.FarmingListener;
import nl.grapjeje.opengrinding.jobs.mining.objects.Ore;
import nl.grapjeje.opengrinding.utils.JobModule;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FarmingModule extends JobModule {
    @Getter
    private static final List<Plant> plants = new ArrayList<>();
    @Getter
    private final static FarmingJobConfiguration config = new FarmingJobConfiguration(OpenGrinding.getInstance().getDataFolder());

    public FarmingModule() {
        super("farming");
    }

    @Override
    protected void onEnable() {
        OpenGrinding.getFramework().registerConfig(config);

        OpenGrinding.getFramework().registerListener(FarmingListener::new);
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

    @Override
    public JobConfig getJobConfig() {
        return config;
    }
}
