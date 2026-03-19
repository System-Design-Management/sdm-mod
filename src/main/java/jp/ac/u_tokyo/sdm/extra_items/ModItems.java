package jp.ac.u_tokyo.sdm.extra_items;

import jp.ac.u_tokyo.sdm.extra_items.item.TechnicalBookItem;
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

    private ModItems() {
    }

    private static Item register(String name, ItemFactory factory) {
        Identifier id = Identifier.of(ExtraItems.MOD_ID, name);
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
