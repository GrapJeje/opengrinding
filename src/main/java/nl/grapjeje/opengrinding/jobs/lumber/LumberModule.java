package nl.grapjeje.opengrinding.jobs.lumber;

import lombok.Getter;
import nl.grapjeje.core.SkullUtil;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.lumber.configuration.LumberJobConfiguration;
import nl.grapjeje.opengrinding.jobs.lumber.listeners.BlockBreakListener;
import nl.grapjeje.opengrinding.jobs.lumber.listeners.BlockChangeListener;
import nl.grapjeje.opengrinding.jobs.lumber.objects.Wood;
import nl.grapjeje.opengrinding.jobs.lumber.timers.WoodTimer;
import nl.grapjeje.opengrinding.utils.JobModule;
import nl.grapjeje.opengrinding.utils.configuration.JobConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class LumberModule extends JobModule {
    @Getter
    private static final List<LumberWood> woods = Collections.synchronizedList(new ArrayList<>());
    @Getter
    private final static LumberJobConfiguration config = new LumberJobConfiguration(OpenGrinding.getInstance().getDataFolder());

    public LumberModule() {
        super("lumber");
    }

    @Override
    protected void onEnable() {
        OpenGrinding.getFramework().registerConfig(config);

        OpenGrinding.getFramework().registerListener(BlockBreakListener::new);
        OpenGrinding.getFramework().registerListener(BlockChangeListener::new);

        new WoodTimer(2);
    }

    @Override
    protected void onDisable() {
        Bukkit.getLogger().info("Replacing all lumber...");
        for (LumberWood wood : woods) {
            if (wood != null && wood.location() != null) {
                if (!wood.location().getChunk().isLoaded())
                    wood.location().getChunk().load();
                wood.location().getBlock().setType(wood.material());
            }
        }
        woods.clear();
    }

    @Override
    public boolean isDisabled() {
        if (!getConfig().isEnabled())
            this.setDisabled();
        return super.isDisabled();
    }

    @Override
    public JobConfig getJobConfig() {
        return config;
    }

    public static ItemStack getWoodHead(Material blockType) {
        Wood matchedWood = null;
        for (Wood wood : Wood.values()) {
            if (blockType == wood.getBarkMaterial() || blockType == wood.getStrippedMaterial()) {
                matchedWood = wood;
                break;
            }
        }
        if (matchedWood == null) return null;
        String link = (blockType == matchedWood.getBarkMaterial()) ? matchedWood.getBarkLink() : matchedWood.getStrippedLink();
        UUID uuid = (blockType == matchedWood.getBarkMaterial()) ? matchedWood.getBarkUUID() : matchedWood.getWoodUUID();

        ItemStack head = SkullUtil.getCustomHead(link, uuid);
        if (head.getItemMeta() instanceof SkullMeta meta) {
            String suffix = (blockType == matchedWood.getBarkMaterial()) ? " bark" : " wood";
            meta.displayName(MessageUtil.filterMessage(matchedWood.getItemName() + suffix));
            head.setItemMeta(meta);
        }
        return head;
    }

    public static record LumberWood(Location location, Material material, long time) {
    }
}
