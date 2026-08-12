package ru.codex.kenmod.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModPayloadRegistrar {
    private ModPayloadRegistrar() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                ApplyNavigationTargetPayload.TYPE,
                ApplyNavigationTargetPayload.STREAM_CODEC,
                ApplyNavigationTargetPayload::handle
        );
    }
}
