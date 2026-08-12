package ru.codex.kenmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import ru.codex.kenmod.KenMod;
import ru.codex.kenmod.block.NavigationWorkbenchBlock;
import ru.codex.kenmod.block.entity.NavigationWorkbenchBlockEntity;

public class NavigationWorkbenchRenderer implements BlockEntityRenderer<NavigationWorkbenchBlockEntity> {
    private static final ResourceLocation POINTER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(KenMod.MOD_ID, "textures/block/navigation_workbench/smart_navigation_table.png");
    private static final float TEX_SIZE = 32.0F;

    public NavigationWorkbenchRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NavigationWorkbenchBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        Direction facing = blockEntity.getBlockState().getValue(NavigationWorkbenchBlock.FACING);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(facing.getRotation());
        poseStack.translate(0.0D, 0.3D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-(blockEntity.getPointerAngleDegrees() + 90.0F)));

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(POINTER_TEXTURE));
        drawBox(consumer, poseStack, packedLight, packedOverlay,
                -1.0F / 16.0F, 0.25F / 16.0F, -7.0F / 16.0F,
                0.94F / 16.0F, 1.19F / 16.0F, -3.06F / 16.0F,
                6.0F, 15.0F, 8.0F, 16.0F);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -0.11D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F));
        drawBox(consumer, poseStack, packedLight, packedOverlay,
                -1.05F / 16.0F, 0.225F / 16.0F, -1.05F / 16.0F,
                1.05F / 16.0F, 1.23F / 16.0F, 1.05F / 16.0F,
                4.0F, 13.0F, 6.5F, 15.0F);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void drawBox(VertexConsumer consumer, PoseStack poseStack, int light, int overlay,
                                float minX, float minY, float minZ,
                                float maxX, float maxY, float maxZ,
                                float minU, float minV, float maxU, float maxV) {
        PoseStack.Pose pose = poseStack.last();
        float u0 = minU / TEX_SIZE;
        float v0 = minV / TEX_SIZE;
        float u1 = maxU / TEX_SIZE;
        float v1 = maxV / TEX_SIZE;

        emitFace(consumer, pose, light, overlay, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, u0, v0, u1, v1, 0.0F, 0.0F, -1.0F);
        emitFace(consumer, pose, light, overlay, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, u0, v0, u1, v1, 0.0F, 0.0F, 1.0F);
        emitFace(consumer, pose, light, overlay, minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, u0, v0, u1, v1, -1.0F, 0.0F, 0.0F);
        emitFace(consumer, pose, light, overlay, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, u0, v0, u1, v1, 1.0F, 0.0F, 0.0F);
        emitFace(consumer, pose, light, overlay, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, u0, v0, u1, v1, 0.0F, 1.0F, 0.0F);
        emitFace(consumer, pose, light, overlay, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, u0, v0, u1, v1, 0.0F, -1.0F, 0.0F);
    }

    private static void emitFace(VertexConsumer consumer, PoseStack.Pose pose, int light, int overlay,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float x3, float y3, float z3,
                                 float x4, float y4, float z4,
                                 float minU, float minV, float maxU, float maxV,
                                 float normalX, float normalY, float normalZ) {
        consumer.addVertex(pose, x1, y1, z1).setColor(255, 255, 255, 255).setUv(minU, maxV).setOverlay(overlay).setLight(light).setNormal(pose, normalX, normalY, normalZ);
        consumer.addVertex(pose, x2, y2, z2).setColor(255, 255, 255, 255).setUv(maxU, maxV).setOverlay(overlay).setLight(light).setNormal(pose, normalX, normalY, normalZ);
        consumer.addVertex(pose, x3, y3, z3).setColor(255, 255, 255, 255).setUv(maxU, minV).setOverlay(overlay).setLight(light).setNormal(pose, normalX, normalY, normalZ);
        consumer.addVertex(pose, x4, y4, z4).setColor(255, 255, 255, 255).setUv(minU, minV).setOverlay(overlay).setLight(light).setNormal(pose, normalX, normalY, normalZ);
    }
}
