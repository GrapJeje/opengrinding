package nl.grapjeje.opengrinding.models;

import com.craftmend.storm.api.StormModel;
import com.craftmend.storm.api.markers.Column;
import com.craftmend.storm.api.markers.Table;
import com.google.gson.Gson;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "fish_loot_table")
public class FishLootTableModel extends StormModel {
    @Getter
    private static final Gson GSON = new Gson();

    @Column(name = "value")
    private String value;

    @Column(name = "chance", defaultValue = "0")
    private Double chance = 0.0;

    @Column(name = "material")
    private String material;

    @Column(name = "metadata")
    private String metadata;

    public ItemStack getItem() {
        if (material == null) return null;

        ItemStack item = new ItemStack(Material.valueOf(material));
        if (metadata != null && !metadata.isEmpty()) {
            Map<String, Object> map = GSON.fromJson(metadata, Map.class);
            item = ItemStack.deserialize(map);
        }
        return item;
    }

    public void setItem(ItemStack item) {
        if (item == null) {
            this.material = null;
            this.metadata = null;
        } else {
            this.material = item.getType().toString();
            Map<String, Object> serialized = item.serialize();
            this.metadata = GSON.toJson(serialized);
        }
    }
}
