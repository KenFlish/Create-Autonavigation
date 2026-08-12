package ru.codex.kenmod;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = KenMod.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = KenMod.MOD_ID, value = Dist.CLIENT)
public class KenModClient {
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        KenMod.LOGGER.info("Client setup complete for {}", Minecraft.getInstance().getUser().getName());
    }
}
