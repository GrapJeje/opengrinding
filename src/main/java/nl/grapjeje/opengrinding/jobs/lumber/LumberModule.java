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
            if (wood != null && wood.location() != null)
                wood.location().getBlock().setType(wood.material());
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
        Wood wood = null;

        for (Wood w : Wood.values()) {
            if (blockType == w.getBarkMaterial() || blockType == w.getStrippedMaterial()) {
                wood = w;
                break;
            }
        }
        if (wood == null) return null;
        String link = (blockType == wood.getBarkMaterial()) ? wood.getBarkLink() : wood.getStrippedLink();

        ItemStack head = SkullUtil.getCustomHead(link, wood.getUuid());
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtil.filterMessage(wood.getItemName() + (blockType == wood.getBarkMaterial() ? " bark" : " wood")));
            head.setItemMeta(meta);
        }
        return head;
    }

    public static record LumberWood(Location location, Material material, long time) {}
}
