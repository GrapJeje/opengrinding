package nl.grapjeje.opengrinding.jobs.farming.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public enum Plant {
    WHEAT("<yellow>Tarwe","https://textures.minecraft.net/texture/f86badb0d913b391fb48d7776c378ca3f4b2dbe724534034f350ccd38f904473");

    private final String itemName;
    private final String link;

    public UUID getUuid() {
        return UUID.nameUUIDFromBytes(link.getBytes(StandardCharsets.UTF_8));
    }
}
