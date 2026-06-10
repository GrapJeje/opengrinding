package nl.grapjeje.opengrinding.jobs.mining.objects;

import lombok.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public enum Ore {
    COAL("<dark_gray>Coal Ore","https://textures.minecraft.net/texture/711107f70f8ca0474f023243bd382bbd6b4149aef4f42b25ddbbcfec8798b4dc"),
    IRON("<white>Iron Ore","https://textures.minecraft.net/texture/8385aaedd784faef8e8f6f782fa48d07c2fc2bbcf6fea1fbc9b9862d05d228c1"),
    COPPER("<dark_orange>Copper Ore","https://textures.minecraft.net/texture/df8ea387b960e9f2a41db323f6de8ec81c652eafa933a4102b96b9ef40c836e5"),
    GOLD("<gold>Gold Ore","https://textures.minecraft.net/texture/e03ab56d48fe815ddc379f706e1615fe5b84be63afda8f8fed3b47678f353cdd"),
    REDSTONE("<red>Redstone Ore","https://textures.minecraft.net/texture/632ccf7814539a61f8bfc15bcf111a39ad8ae163c36e44b6379415556475d72a"),
    LAPIS("<blue>Lapis Ore","https://textures.minecraft.net/texture/51001b425111bfe0acff710a8b41ea95e3b936a85e5bb6517160bab587e8870f"),
    DIAMOND("<aqua>Diamond Ore","https://textures.minecraft.net/texture/8211549c90b99dac16f5f709b66d91830300bb97f3b65c7c55a2fd62ace632fc"),
    EMERALD("<green>Emerald Ore","https://textures.minecraft.net/texture/98c63b717e16c9a889b09235d196f42d4c31fbba5d2205e1a95f055e69ba4835");

    private final String itemName;
    private final String link;

    public UUID getUuid() {
        return UUID.nameUUIDFromBytes(link.getBytes(StandardCharsets.UTF_8));
    }
}
