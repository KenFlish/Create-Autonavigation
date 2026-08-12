package ru.codex.kenmod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import ru.codex.kenmod.KenMod;
import ru.codex.kenmod.block.entity.NavigationWorkbenchBlockEntity;
import ru.codex.kenmod.menu.NavigationWorkbenchMenu;

public record ApplyNavigationTargetPayload(BlockPos blockPos, int targetX, int targetY, int targetZ)
        implements CustomPacketPayload {
    public static final Type<ApplyNavigationTargetPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(KenMod.MOD_ID, "apply_navigation_target"));
    public static final StreamCodec<ByteBuf, ApplyNavigationTargetPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ApplyNavigationTargetPayload::blockPos,
            ByteBufCodecs.INT,
            ApplyNavigationTargetPayload::targetX,
            ByteBufCodecs.INT,
            ApplyNavigationTargetPayload::targetY,
            ByteBufCodecs.INT,
            ApplyNavigationTargetPayload::targetZ,
            ApplyNavigationTargetPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ApplyNavigationTargetPayload payload, IPayloadContext context) {
        if (context.player().containerMenu instanceof NavigationWorkbenchMenu menu
                && menu.getBlockPos().equals(payload.blockPos())
                && context.player().level().getBlockEntity(payload.blockPos()) instanceof NavigationWorkbenchBlockEntity workbench) {
            workbench.setTarget(payload.targetX(), payload.targetY(), payload.targetZ());
        }
    }
}
