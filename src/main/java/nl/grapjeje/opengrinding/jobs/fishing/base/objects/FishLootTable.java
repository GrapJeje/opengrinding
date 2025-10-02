package nl.grapjeje.opengrinding.jobs.fishing.base.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.grapjeje.opengrinding.models.FishLootTableModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
public class FishLootTable {
    private final FishLootTableModel model;

    public CompletableFuture<Void> save() {
        return CompletableFuture.runAsync(() -> StormDatabase.getInstance().saveStormModel(model));
    }

    public String getValue() {
        return model.getValue();
    }

    public void setValue(String value) {
        model.setValue(value);
    }

    public Double getChance() {
        return model.getChance();
    }

    public void setChance(Double chance) {
        model.setChance(chance);
    }

    public String getMaterial() {
        return model.getMaterial();
    }

    public void setMaterial(String material) {
        model.setMaterial(material);
    }

    public String getMetadata() {
        return model.getMetadata();
    }

    public void setMetadata(String metadata) {
        model.setMetadata(metadata);
    }

    public ItemStack getItem() {
        return model.getItem();
    }

    public void setItem(ItemStack item) {
        model.setItem(item);
    }
}
