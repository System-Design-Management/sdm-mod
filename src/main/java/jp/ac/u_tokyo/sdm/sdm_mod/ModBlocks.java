package jp.ac.u_tokyo.sdm.sdm_mod;

import jp.ac.u_tokyo.sdm.sdm_mod.block.SearchPcBlock;
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

    public static final Block SEARCH_PC = register(
        "search_pc",
        AbstractBlock.Settings.create().strength(1.0f),
        SearchPcBlock::new
    );

    private ModBlocks() {
    }

    private static Block register(String name, AbstractBlock.Settings settings) {
        return register(name, settings, Block::new);
    }

    private static Block register(String name, AbstractBlock.Settings settings, java.util.function.Function<AbstractBlock.Settings, Block> factory) {
        Identifier id = Identifier.of(SdmMod.MOD_ID, name);
        RegistryKey<Block> key = RegistryKey.of(Registries.BLOCK.getKey(), id);
        return Registry.register(Registries.BLOCK, key, factory.apply(settings.registryKey(key)));
    }

    public static void initialize() {
    }
}
