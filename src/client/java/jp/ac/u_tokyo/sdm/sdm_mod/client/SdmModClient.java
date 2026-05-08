package jp.ac.u_tokyo.sdm.sdm_mod.client;

import jp.ac.u_tokyo.sdm.sdm_mod.ModEntities;
import jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.PoliceOfficerEntityRenderer;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.TechnicalBookScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.warp.WarpSelectScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class SdmModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.POLICE_OFFICER, PoliceOfficerEntityRenderer::new);
        HandledScreens.register(ModScreenHandlers.TECHNICAL_BOOK, TechnicalBookScreen::new);
        HandledScreens.register(ModScreenHandlers.WARP_SELECT, WarpSelectScreen::new);
    }
}
