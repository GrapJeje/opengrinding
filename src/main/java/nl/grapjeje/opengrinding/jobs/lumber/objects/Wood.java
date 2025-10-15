package nl.grapjeje.opengrinding.jobs.lumber.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public enum Wood {
    OAK("<gold>Oak", "https://textures.minecraft.net/texture/b714025a557e333a9d955ea9b3b2111119a89e13ebc1e294e300609c6b3364c", "https://textures.minecraft.net/texture/594e7d16ea961c2d0159b46ea109fadffaddaccf39daa4423633b4a37b43e2f8"),
    SPRUCE("<gray>Spruce", "https://textures.minecraft.net/texture/faf05c4e4224fddd0b502a1e6794cc3e5f1fadfc5407da5338a825c04ae9ced1", "https://textures.minecraft.net/texture/fbf61fd94c49d333a22d884846e247ae644d228d2eedd4f4e3b99f19ab8d3056"),
    BIRCH("<yellow>Birch", "https://textures.minecraft.net/texture/b76c7f96f862243c5a6fe727aec0b8657cd2c65a463fd816c94efe4c622c055a", "https://textures.minecraft.net/texture/f8fd63fa7afb40296f6aac0db367bdbdc6d75ed707b0b16e12a371301921ad09"),
    JUNGLE("<gold>Jungle", "https://textures.minecraft.net/texture/3e9fe76c104295ea111c7a33d70cc2cf58139be1769bf72a09b70975127fa896", "https://textures.minecraft.net/texture/7867a7b1151ba562e4aed570901d352490a077d59d1711fda294d5c813e2b5bc"),
    ACACIA("<red>Acacia", "https://textures.minecraft.net/texture/4a4c99a3bc821f8b1df8bc732e03e0f3afa3265f53f9c9ece3ef67b54d65d7bf", "https://textures.minecraft.net/texture/65e7a2a0e0de05397174109d54d0afa64472104054b2ab045283411836bea2e4"),
    DARK_OAK("<dark_gray>Dark Oak", "https://textures.minecraft.net/texture/6813ad845c3026bf01e0dd89b2915de8cb734ce558a600023b61c2d49ffae89d", "https://textures.minecraft.net/texture/8db7b1ef15496fdb1cb4c3eacb0c03fff111db6daa9e8424f78a0775f962cc73"),
    MANGROVE("<dark_red>Mangrove", "https://textures.minecraft.net/texture/43debb29acbdb981b598cdcb6e3f2a508c4bcc7baac509927f40af7560ffcc2c", "https://textures.minecraft.net/texture/9a4a154ba270f4c3dbd8c5e1658926ce06b5ab9609a16d71720e890e7d5e6d9"),
    CHERRY("<light_purple>Cherry", "https://textures.minecraft.net/texture/ff6718990bce5c2f5c25da3f2908dbeab133f52195ddf04f5251b25fd9edd960", "https://textures.minecraft.net/texture/a26e7d55b4e64111892d990ab14b8117cfdbcc457ad4ff78c47b1ac3a8b8761b"),
    CRIMSON("<dark_red>Crimson", "https://textures.minecraft.net/texture/52bf59f03cf4120fb16313c6499fbf9252a49b831dec6e17647aafcee1bde4bd", "https://textures.minecraft.net/texture/b239b5d8626506bc8a4866575df801d4cade6b3578afe84ff602763fd9d1fc5f"),
    WARPED("<dark_aqua>Warped", "https://textures.minecraft.net/texture/9753a49703c068bb182d7660a905ce5eb9549ec0e6461ceae5dd0614433a998b", "https://textures.minecraft.net/texture/f77cc91c6aae64473613a5d7c81543f18639fb879ee5e234eda6fdc9ac6ae30e"),
    PALE_OAK("<white>Pale Oak", "https://textures.minecraft.net/texture/a619c2ce6dbddacbf3f48f7500732a8aa4145ace28f3a9260cffa4fde17a42f", "https://textures.minecraft.net/texture/76288b187326098949f93d2244859a1e827c0886c5f233c3b8f1dcc249e204dc");

    private final String itemName;
    private final String barkLink;
    private final String strippedLink;

    public UUID getUuid() {
        return UUID.nameUUIDFromBytes(barkLink.getBytes(StandardCharsets.UTF_8));
    }

    public Material getBarkMaterial() {
        return switch (this) {
            case CRIMSON -> Material.CRIMSON_HYPHAE;
            case WARPED -> Material.WARPED_HYPHAE;
            default -> Material.valueOf(this.name() + "_WOOD");
        };
    }

    public Material getStrippedMaterial() {
        return switch (this) {
            case CRIMSON -> Material.STRIPPED_CRIMSON_HYPHAE;
            case WARPED -> Material.STRIPPED_WARPED_HYPHAE;
            default -> Material.valueOf("STRIPPED_" + this.name() + "_WOOD");
        };
    }
}
