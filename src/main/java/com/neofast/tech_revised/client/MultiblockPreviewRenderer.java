package com.neofast.tech_revised.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neofast.tech_revised.multiblock.MultiblockStructureBlueprint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class MultiblockPreviewRenderer {
    private static final long PREVIEW_DURATION_MS = 15_000L;
    private static final List<ActivePreview> PREVIEWS = new ArrayList<>();

    private MultiblockPreviewRenderer() {
    }

    public static void register() {
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            render(context.matrixStack(), context.camera().getPosition());
        });
    }

    public static void previewInFrontOfPlayer(MultiblockStructureBlueprint blueprint) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || blueprint == null) {
            return;
        }
        Direction facing = mc.player.getDirection();
        BlockPos origin;
        if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit
                && hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            origin = hit.getBlockPos().relative(hit.getDirection());
        } else {
            origin = mc.player.blockPosition().relative(facing, 3).above();
        }
        showPreview(origin, facing.getOpposite(), blueprint);
        mc.player.displayClientMessage(Component.translatable("message.tech_revised.multiblock.hologram"), true);
    }

    public static void showPreview(BlockPos controllerPos, Direction front, MultiblockStructureBlueprint blueprint) {
        if (blueprint == null) {
            return;
        }
        List<GhostBlock> ghosts = new ArrayList<>();
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (MultiblockStructureBlueprint.Cell cell : blueprint.getCells()) {
            if (cell.x() == 0 && cell.y() == 0 && cell.z() == 0) {
                continue;
            }
            BlockPos worldPos = localToWorld(controllerPos, front, cell.x(), cell.y(), cell.z());
            Block block = Block.byItem(cell.stack().getItem());
            ghosts.add(new GhostBlock(worldPos, block.defaultBlockState(), cell.y()));
            minY = Math.min(minY, cell.y());
            maxY = Math.max(maxY, cell.y());
        }
        if (ghosts.isEmpty()) {
            return;
        }
        PREVIEWS.removeIf(p -> p.controllerPos.equals(controllerPos));
        long now = System.currentTimeMillis();
        PREVIEWS.add(new ActivePreview(controllerPos, ghosts, minY, maxY, now, now + PREVIEW_DURATION_MS));
    }

    public static void clearAll() {
        PREVIEWS.clear();
    }

    private static void render(PoseStack poseStack, net.minecraft.world.phys.Vec3 camera) {
        if (PREVIEWS.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        PREVIEWS.removeIf(p -> p.expireAtMs <= now);
        if (PREVIEWS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        for (ActivePreview preview : PREVIEWS) {
            for (GhostBlock ghost : preview.ghosts) {
                poseStack.pushPose();
                poseStack.translate(ghost.pos.getX(), ghost.pos.getY(), ghost.pos.getZ());
                try {
                    dispatcher.renderSingleBlock(ghost.state, poseStack, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY);
                } catch (Exception ignored) {
                }
                poseStack.popPose();
            }
        }
        buffers.endBatch();
        poseStack.popPose();
    }

    public static BlockPos localToWorld(BlockPos origin, Direction front, int localX, int localY, int localZ) {
        Direction right = front.getClockWise();
        Direction back = front.getOpposite();
        return origin.relative(right, localX).relative(back, localZ).offset(0, localY, 0);
    }

    private record GhostBlock(BlockPos pos, BlockState state, int localY) {
    }

    private record ActivePreview(BlockPos controllerPos, List<GhostBlock> ghosts, int minY, int maxY,
                                 long startMs, long expireAtMs) {
    }
}
