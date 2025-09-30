package nl.grapjeje.opengrinding.jobs.mining;

import lombok.Getter;
import nl.grapjeje.core.SkullUtil;
import nl.grapjeje.core.modules.Module;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.jobs.mining.listener.BlockBreakListener;
import nl.grapjeje.opengrinding.jobs.mining.objects.MiningOres;
import nl.grapjeje.opengrinding.jobs.mining.objects.Ore;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class MiningModule extends Module {
    @Getter
    private final static List<MiningOres> ores = new ArrayList<>();

    public MiningModule() {
        super("mining");
    }

    @Override
    protected void onEnable() {
        OpenGrinding.getFramework().registerListener(BlockBreakListener::new);
    }

    @Override
    protected void onDisable() {

    }

    public static ItemStack getBlockHead(Block brokenBlock) {
        Material blockType = brokenBlock.getType();

        String enumName = blockType.name().replace("_ORE", "");
        Ore ore;
        try {
            ore = Ore.valueOf(enumName);
        } catch (IllegalArgumentException e) {
            return null;
        }

        ItemStack head = SkullUtil.getCustomHead(ore.getLink(), ore.getUuid());
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtil.filterMessage(ore.getItemName()));
            head.setItemMeta(meta);
        }
        return head;
    }
}
