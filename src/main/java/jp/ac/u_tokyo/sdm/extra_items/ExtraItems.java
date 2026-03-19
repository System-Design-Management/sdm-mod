package jp.ac.u_tokyo.sdm.extra_items;

import jp.ac.u_tokyo.sdm.extra_items.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;

public class ExtraItems implements ModInitializer {
    public static final String MOD_ID = "extra_items";

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModScreenHandlers.initialize();
        ModItems.initialize();
    }
}
