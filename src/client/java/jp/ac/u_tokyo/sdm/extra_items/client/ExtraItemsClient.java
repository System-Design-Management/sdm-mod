package jp.ac.u_tokyo.sdm.extra_items.client;

import jp.ac.u_tokyo.sdm.extra_items.client.screen.TechnicalBookScreen;
import jp.ac.u_tokyo.sdm.extra_items.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class ExtraItemsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.TECHNICAL_BOOK, TechnicalBookScreen::new);
    }
}
