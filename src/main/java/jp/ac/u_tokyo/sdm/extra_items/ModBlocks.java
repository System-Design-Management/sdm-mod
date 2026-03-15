package jp.ac.u_tokyo.sdm.extra_items;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    public static final Block TECHNICAL_BOOK = register(
        "technical_book",
        AbstractBlock.Settings.create().strength(1.0f)
    );

    private ModBlocks() {
    }

    private static Block register(String name, AbstractBlock.Settings settings) {
        Identifier id = Identifier.of(ExtraItems.MOD_ID, name);
        RegistryKey<Block> key = RegistryKey.of(Registries.BLOCK.getKey(), id);
        return Registry.register(Registries.BLOCK, key, new Block(settings.registryKey(key)));
    }

    public static void initialize() {
    }
}
