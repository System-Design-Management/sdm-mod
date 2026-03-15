package jp.ac.u_tokyo.sdm.extra_items;

import net.fabricmc.api.ModInitializer;

public class ExtraItems implements ModInitializer {
    public static final String MOD_ID = "extra_items";

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModItems.initialize();
    }
}
