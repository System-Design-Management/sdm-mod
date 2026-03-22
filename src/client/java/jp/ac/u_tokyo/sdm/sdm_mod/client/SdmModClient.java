package jp.ac.u_tokyo.sdm.sdm_mod.client;

import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.TechnicalBookScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class SdmModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.TECHNICAL_BOOK, TechnicalBookScreen::new);
    }
}
