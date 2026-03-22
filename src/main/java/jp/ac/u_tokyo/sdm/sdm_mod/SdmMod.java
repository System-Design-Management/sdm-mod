package jp.ac.u_tokyo.sdm.sdm_mod;

import jp.ac.u_tokyo.sdm.sdm_mod.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;

public class SdmMod implements ModInitializer {
    public static final String MOD_ID = "sdm_mod";

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModScreenHandlers.initialize();
        ModItems.initialize();
    }
}
