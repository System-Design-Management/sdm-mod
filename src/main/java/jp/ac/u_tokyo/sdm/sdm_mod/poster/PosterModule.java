package jp.ac.u_tokyo.sdm.sdm_mod.poster;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.util.Identifier;

public final class PosterModule {
    private PosterModule() {}

    public static void initialize() {
        registerPosters();
        PosterCommandInitializer.initialize();
    }

    private static void registerPosters() {
        for (int i = 0; i <= 19; i++) {
            PosterRegistry.register(
                "classification" + i,
                Identifier.of(SdmMod.MOD_ID, "textures/gui/poster/classification_" + i + ".png"),
                0.95f, 0.3f
            );
        }
    }
}
