package nl.grapjeje.opengrinding.jobs.fishing.objects;

import com.craftmend.storm.api.enums.Where;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import nl.grapjeje.core.text.MessageUtil;
import nl.grapjeje.opengrinding.OpenGrinding;
import nl.grapjeje.opengrinding.models.FishLootTableModel;
import nl.openminetopia.modules.data.storm.StormDatabase;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    public static CompletableFuture<LootResult> getRandomItem(String value) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Collection<FishLootTableModel> modelsCollection = StormDatabase.getInstance().getStorm()
                        .buildQuery(FishLootTableModel.class)
                        .where("value", Where.EQUAL, value)
                        .execute()
                        .join();

                List<FishLootTable> lootItems = modelsCollection.stream()
                        .map(FishLootTable::new)
                        .toList();
                if (lootItems.isEmpty())
                    return new LootResult(null, "<warning>⚠ Geen loot items gevonden voor value '" + value + "'.");

                double totalChance = lootItems.stream().mapToDouble(FishLootTable::getChance).sum();
                if (Math.abs(totalChance - 100.0) > 0.001) {
                    OpenGrinding.getInstance().getLogger().severe("Total chance for value '" + value + "' is " + totalChance + "%, needs to be 100%!");
                    return new LootResult(null, "<warning>⚠ Totale kans voor value '" + value + "' is " + totalChance + "%, moet 100% zijn!");
                }
                double random = new Random().nextDouble() * totalChance;
                double cumulative = 0;
                FishLootTable selectedLoot = null;
                for (FishLootTable loot : lootItems) {
                    cumulative += loot.getChance();
                    if (random <= cumulative) {
                        selectedLoot = loot;
                        break;
                    }
                }
                if (selectedLoot == null) return new LootResult(null, "<warning>⚠ Er is geen loot geselecteerd.");

                ItemStack item = selectedLoot.getItem();
                if (item == null) return new LootResult(null, "<warning>⚠ Het geselecteerde loot-item is null.");

                Material type = item.getType();
                if (type == Material.SALMON || type == Material.COD || type == Material.TROPICAL_FISH) {
                    double gewicht = Math.round((0.5 + new Random().nextDouble() * 4.5) * 10.0) / 10.0;

                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
                        lore.add(MessageUtil.filterMessage("<gray>Gewicht: " + gewicht + "kg"));
                        meta.lore(lore);

                        NamespacedKey key = new NamespacedKey("opengrinding", "fish_weight");
                        meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, gewicht);
                        item.setItemMeta(meta);
                    }
                }
                return new LootResult(item, null);
            } catch (Exception e) {
                e.printStackTrace();
                return new LootResult(null, "<warning>⚠ Er is een exception opgetreden: " + e.getMessage());
            }
        });
    }

    public record LootResult(ItemStack item, String errorMessage) {
        public boolean hasError() {
            return errorMessage != null && !errorMessage.isEmpty();
        }
    }

    public static CompletableFuture<Map<String, Boolean>> checkChancesPerValue() {
        return CompletableFuture.supplyAsync(() -> {
            Collection<FishLootTableModel> modelsCollection;
            try {
                modelsCollection = StormDatabase.getInstance().getStorm()
                        .buildQuery(FishLootTableModel.class)
                        .execute()
                        .join();
            } catch (Exception ex) {
                ex.printStackTrace();
                return Map.of();
            }
            List<FishLootTableModel> models = List.copyOf(modelsCollection);
            if (models.isEmpty()) return Map.of();

            Map<String, Double> totals = models.stream()
                    .collect(Collectors.groupingBy(
                            FishLootTableModel::getValue,
                            Collectors.summingDouble(FishLootTableModel::getChance)
                    ));
            return totals.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> Math.abs(e.getValue() - 100.0) < 0.001
                    ));
        });
    }
}
