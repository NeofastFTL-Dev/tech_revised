package com.neofast.tech_revised.multiblock;

import com.neofast.tech_revised.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MachineMultiblockHelper {
    private MachineMultiblockHelper() {
    }

    public static InteractionResult handleUse(Level level, BlockPos pos, Player player, InteractionHand hand,
                                              Direction front, MenuProvider menu) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND
                && !held.is(ModItems.CONFIGURATOR.get())
                && player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.CONFIGURATOR.get())) {
            return InteractionResult.PASS;
        }

        MachineLayout layout = MachineLayouts.get(level.getBlockState(pos).getBlock());
        if (layout == null) {
            MultiblockAutoBuilder.openScreen(player, (BlockEntity) menu);
            return InteractionResult.CONSUME;
        }

        if (held.is(ModItems.CONFIGURATOR.get())) {
            if (!MultiblockAutoBuilder.tryBuild(level, player, layout.placements(pos, front))) {
                return InteractionResult.CONSUME;
            }
            boolean formed = layout.isValid(level, pos, front);
            layout.updateFormedStates(level, pos, front, formed);
            player.displayClientMessage(Component.translatable(formed
                    ? "message.tech_revised.multiblock.autoconfig_success"
                    : "message.tech_revised.multiblock.autoconfig_incomplete"), true);
            if (formed) {
                MultiblockAutoBuilder.openScreen(player, level.getBlockEntity(pos));
            }
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            MultiblockPreviewHelper.showPreview(player, pos, front, layout.getId());
            player.displayClientMessage(Component.translatable("message.tech_revised.multiblock.hologram"), true);
            return InteractionResult.CONSUME;
        }

        boolean formed = layout.isValid(level, pos, front);
        layout.updateFormedStates(level, pos, front, formed);
        if (!formed) {
            player.displayClientMessage(Component.translatable(layout.getIncompleteKey()), true);
            MultiblockPreviewHelper.showPreview(player, pos, front, layout.getId());
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.translatable(layout.getFormedKey()), true);
        MultiblockAutoBuilder.openScreen(player, level.getBlockEntity(pos));
        return InteractionResult.CONSUME;
    }
}
