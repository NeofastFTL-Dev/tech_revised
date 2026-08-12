package com.neofast.tech_revised.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neofast.tech_revised.integration.jei.MultiblockStructureBlueprint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Client-side translucent multiblock structure preview, inspired by GregTech's
 * in-world structure hologram when interacting with a controller.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MultiblockPreviewRenderer {
    private static final long PREVIEW_DURATION_MS = 12_000L;
    private static final float GHOST_ALPHA = 0.45f;

    private static final List<ActivePreview> PREVIEWS = new ArrayList<>();

    private MultiblockPreviewRenderer() {
    }

    public static void showPreview(BlockPos controllerPos, Direction front, MultiblockStructureBlueprint blueprint) {
        if (blueprint == null) {
            return;
        }

        List<GhostBlock> ghosts = new ArrayList<>();
        for (MultiblockStructureBlueprint.Cell cell : blueprint.getCells()) {
            if (cell.x() == 0 && cell.y() == 0 && cell.z() == 0) {
                continue; // skip controller itself
            }
            BlockPos worldPos = localToWorld(controllerPos, front, cell.x(), cell.y(), cell.z());
            Block block = Block.byItem(cell.stack().getItem());
            if (block == null) {
                continue;
            }
            ghosts.add(new GhostBlock(worldPos, block.defaultBlockState()));
        }

        // Replace any existing preview at the same controller
        PREVIEWS.removeIf(p -> p.controllerPos.equals(controllerPos));
        PREVIEWS.add(new ActivePreview(controllerPos, ghosts, System.currentTimeMillis() + PREVIEW_DURATION_MS));
    }

    public static void clearAll() {
        PREVIEWS.clear();
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (PREVIEWS.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        Iterator<ActivePreview> it = PREVIEWS.iterator();
        while (it.hasNext()) {
            if (it.next().expireAtMs <= now) {
                it.remove();
            }
        }
        if (PREVIEWS.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        var camera = event.getCamera().getPosition();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RandomSource random = RandomSource.create();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        for (ActivePreview preview : PREVIEWS) {
            for (GhostBlock ghost : preview.ghosts) {
                // Skip positions that already have the correct block
                if (mc.level.getBlockState(ghost.pos).is(ghost.state.getBlock())) {
                    continue;
                }

                poseStack.pushPose();
                poseStack.translate(ghost.pos.getX(), ghost.pos.getY(), ghost.pos.getZ());

                // Slight scale so ghost doesn't z-fight with existing geometry
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(0.98f, 0.98f, 0.98f);
                poseStack.translate(-0.5, -0.5, -0.5);

                VertexConsumer consumer = buffers.getBuffer(RenderType.translucent());
                try {
                    dispatcher.renderSingleBlock(
                            ghost.state,
                            poseStack,
                            buffers,
                            0xF000F0,
                            OverlayTexture.NO_OVERLAY,
                            ModelData.EMPTY,
                            RenderType.translucent()
                    );
                } catch (Exception ignored) {
                    // Some blocks may not like translucent pass; fall back to outline
                    renderOutlineBox(poseStack, buffers, ghost.state);
                }

                poseStack.popPose();
            }
        }

        buffers.endBatch(RenderType.translucent());
        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void renderOutlineBox(PoseStack poseStack, MultiBufferSource buffers, BlockState state) {
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();
        float r = 0.2f, g = 0.85f, b = 1.0f, a = 0.8f;
        // 12 edges of unit cube
        line(lines, matrix, 0, 0, 0, 1, 0, 0, r, g, b, a);
        line(lines, matrix, 0, 1, 0, 1, 1, 0, r, g, b, a);
        line(lines, matrix, 0, 0, 1, 1, 0, 1, r, g, b, a);
        line(lines, matrix, 0, 1, 1, 1, 1, 1, r, g, b, a);
        line(lines, matrix, 0, 0, 0, 0, 1, 0, r, g, b, a);
        line(lines, matrix, 1, 0, 0, 1, 1, 0, r, g, b, a);
        line(lines, matrix, 0, 0, 1, 0, 1, 1, r, g, b, a);
        line(lines, matrix, 1, 0, 1, 1, 1, 1, r, g, b, a);
        line(lines, matrix, 0, 0, 0, 0, 0, 1, r, g, b, a);
        line(lines, matrix, 1, 0, 0, 1, 0, 1, r, g, b, a);
        line(lines, matrix, 0, 1, 0, 0, 1, 1, r, g, b, a);
        line(lines, matrix, 1, 1, 0, 1, 1, 1, r, g, b, a);
    }

    private static void line(VertexConsumer consumer, Matrix4f matrix,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float r, float g, float b, float a) {
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(0, 1, 0).endVertex();
    }

    /** Same coordinate convention as controller blocks / MultiblockStructureBlueprint. */
    public static BlockPos localToWorld(BlockPos origin, Direction front, int localX, int localY, int localZ) {
        Direction right = front.getClockWise();
        Direction back = front.getOpposite();
        return origin.relative(right, localX).relative(back, localZ).offset(0, localY, 0);
    }

    private record GhostBlock(BlockPos pos, BlockState state) {
    }

    private record ActivePreview(BlockPos controllerPos, List<GhostBlock> ghosts, long expireAtMs) {
    }
}
