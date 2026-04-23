package jp.ac.u_tokyo.sdm.sdm_mod;

import jp.ac.u_tokyo.sdm.sdm_mod.item.TechnicalBookItem;
import jp.ac.u_tokyo.sdm.sdm_mod.item.WarpItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item STUDENT_ID = register(
        "student_id",
        settings -> new Item(settings)
    );

    public static final Item TECHNICAL_BOOK = register(
        "technical_book",
        settings -> new TechnicalBookItem(ModBlocks.TECHNICAL_BOOK, settings)
    );

    public static final Item ICHO_ITEM = register(
        "icho_item",
        settings -> new Item(settings)
    );

    public static final Item KEY_BOOK = register(
        "key_book",
        settings -> new Item(settings)
    );

    public static final Item WARP_TABLET = register(
        "warp_tablet",
        WarpItem::new
    );

    public static final Item SEARCH_PC = register(
        "search_pc",
        settings -> new BlockItem(ModBlocks.SEARCH_PC, settings)
    );

    private ModItems() {
    }

    private static Item register(String name, ItemFactory factory) {
        Identifier id = Identifier.of(SdmMod.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(Registries.ITEM.getKey(), id);
        Item.Settings settings = new Item.Settings().registryKey(key);
        return Registry.register(Registries.ITEM, key, factory.create(settings));
    }

    public static void initialize() {
    }

    @FunctionalInterface
    private interface ItemFactory {
        Item create(Item.Settings settings);
    }
}
