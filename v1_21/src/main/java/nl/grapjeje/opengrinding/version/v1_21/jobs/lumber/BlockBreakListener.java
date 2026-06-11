package nl.grapjeje.opengrinding.version.v1_21.jobs.lumber;

import nl.grapjeje.opengrinding.shared.jobs.lumber.listeners.BaseBlockBreakListener;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public class BlockBreakListener extends BaseBlockBreakListener {

    private static final Set<Material> WHITELIST = createWhitelist();

    @Override
    protected Set<Material> getWhitelist() {
        return WHITELIST;
    }

    private static Set<Material> createWhitelist() {
        Set<Material> set = new HashSet<>();
        set.add(Material.OAK_WOOD);
        set.add(Material.STRIPPED_OAK_WOOD);
        set.add(Material.SPRUCE_WOOD);
        set.add(Material.STRIPPED_SPRUCE_WOOD);
        set.add(Material.BIRCH_WOOD);
        set.add(Material.STRIPPED_BIRCH_WOOD);
        set.add(Material.JUNGLE_WOOD);
        set.add(Material.STRIPPED_JUNGLE_WOOD);
        set.add(Material.ACACIA_WOOD);
        set.add(Material.STRIPPED_ACACIA_WOOD);
        set.add(Material.DARK_OAK_WOOD);
        set.add(Material.STRIPPED_DARK_OAK_WOOD);
        set.add(Material.MANGROVE_WOOD);
        set.add(Material.STRIPPED_MANGROVE_WOOD);
        set.add(Material.CHERRY_WOOD);
        set.add(Material.STRIPPED_CHERRY_WOOD);
        set.add(Material.CRIMSON_HYPHAE);
        set.add(Material.STRIPPED_CRIMSON_HYPHAE);
        set.add(Material.WARPED_HYPHAE);
        set.add(Material.STRIPPED_WARPED_HYPHAE);
        return Set.copyOf(set);
    }
}
