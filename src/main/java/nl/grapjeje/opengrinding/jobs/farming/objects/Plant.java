package nl.grapjeje.opengrinding.jobs.farming.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public enum Plant {
    WHEAT("<yellow>Tarwe","https://textures.minecraft.net/texture/f86badb0d913b391fb48d7776c378ca3f4b2dbe724534034f350ccd38f904473"),
    BEETROOT("<dark_red>Bietje", "https://textures.minecraft.net/texture/a87e0d5c682d197b499ffc8780715daee196b91284a4f3498dee763ca1da47c0"),
    CARROT("<dark_orange>Wortel", "https://textures.minecraft.net/texture/4d3a6bd98ac1833c664c4909ff8d2dc62ce887bdcf3cc5b3848651ae5af6b"),
    POTATO("<bronze>Aardappel", "https://textures.minecraft.net/texture/a87e0d5c682d197b499ffc8780715daee196b91284a4f3498dee763ca1da47c0");

    private final String itemName;
    private final String link;

    public UUID getUuid() {
        return UUID.nameUUIDFromBytes(link.getBytes(StandardCharsets.UTF_8));
    }
}
