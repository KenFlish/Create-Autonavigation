package ru.codex.kenmod.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import ru.codex.kenmod.KenMod;
import ru.codex.kenmod.client.renderer.NavigationWorkbenchRenderer;
import ru.codex.kenmod.client.screen.NavigationWorkbenchScreen;

@EventBusSubscriber(modid = KenMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(KenMod.NAVIGATION_WORKBENCH_BLOCK_ENTITY.get(), NavigationWorkbenchRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(KenMod.NAVIGATION_WORKBENCH_MENU.get(), NavigationWorkbenchScreen::new);
    }
}
