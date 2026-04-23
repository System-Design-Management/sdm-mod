package jp.ac.u_tokyo.sdm.sdm_mod.screen;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public final class ModScreenHandlers {
    public static final ScreenHandlerType<TechnicalBookScreenHandler> TECHNICAL_BOOK = register(
        "technical_book",
        TechnicalBookScreenHandler::new
    );
    public static final ScreenHandlerType<WarpSelectScreenHandler> WARP_SELECT = register(
        "warp_select",
        WarpSelectScreenHandler::new
    );

    private ModScreenHandlers() {
    }

    private static <T extends net.minecraft.screen.ScreenHandler> ScreenHandlerType<T> register(
        String name,
        ScreenHandlerType.Factory<T> factory
    ) {
        Identifier id = Identifier.of(SdmMod.MOD_ID, name);
        RegistryKey<ScreenHandlerType<?>> key = RegistryKey.of(Registries.SCREEN_HANDLER.getKey(), id);
        return Registry.register(
            Registries.SCREEN_HANDLER,
            key,
            new ScreenHandlerType<>(factory, FeatureFlags.DEFAULT_ENABLED_FEATURES)
        );
    }

    public static void initialize() {
    }
}
