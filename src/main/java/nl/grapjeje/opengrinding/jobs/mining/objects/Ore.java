package nl.grapjeje.opengrinding.jobs.mining.objects;

import lombok.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public enum Ore {
    COAL("<dark_gray>Coal Ore","https://textures.minecraft.net/texture/711107f70f8ca0474f023243bd382bbd6b4149aef4f42b25ddbbcfec8798b4dc"),
    IRON("<white>Iron Ore","https://textures.minecraft.net/texture/8385aaedd784faef8e8f6f782fa48d07c2fc2bbcf6fea1fbc9b9862d05d228c1"),
    COPPER("<dark_orange>Copper Ore","https://textures.minecraft.net/texture/8385aaedd784faef8e8f6f782fa48d07c2fc2bbcf6fea1fbc9b9862d05d228c1"), // TODO: FIND
    GOLD("<gold>Gold Ore","https://textures.minecraft.net/texture/e03ab56d48fe815ddc379f706e1615fe5b84be63afda8f8fed3b47678f353cdd"),
    REDSTONE("<red>Redstone Ore","https://textures.minecraft.net/texture/e03ab56d48fe815ddc379f706e1615fe5b84be63afda8f8fed3b47678f353cdd"), // TODO: FIND
    LAPIS("<blue>Lapis Ore","https://textures.minecraft.net/texture/e03ab56d48fe815ddc379f706e1615fe5b84be63afda8f8fed3b47678f353cdd"), // TODO: FIND
    DIAMOND("<aqua>Diamond Ore","https://textures.minecraft.net/texture/8211549c90b99dac16f5f709b66d91830300bb97f3b65c7c55a2fd62ace632fc"),
    EMERALD("<green>Emerald Ore","https://textures.minecraft.net/texture/e03ab56d48fe815ddc379f706e1615fe5b84be63afda8f8fed3b47678f353cdd"); // TODO: FIND

    private final String itemName;
    private final String link;

    public UUID getUuid() {
        return UUID.nameUUIDFromBytes(link.getBytes(StandardCharsets.UTF_8));
    }
}
