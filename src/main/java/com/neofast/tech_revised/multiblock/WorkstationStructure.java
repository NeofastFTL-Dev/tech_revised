package com.neofast.tech_revised.multiblock;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.custom.WorkstationKitBlock;
import com.neofast.tech_revised.block.custom.WorkstationMonitorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Desk-scene workstation layout. The monitor controller sits at (0,0,0) on the
 * desk surface; every other desk block carries a local desk-slab model so the
 * peripherals visually rest on the same surface. Positive local Z is behind the
 * monitor. Keyboard/mouse/HDMI sit in front (negative Z), speakers behind.
 * Legs fill the layer below, the tower sits under the keyboard, the power
 * strip sits behind the HDMI side, and the chair is tucked in front.
 */
public final class WorkstationStructure {
    public record Part(int x, int y, int z, java.util.function.Supplier<Block> block, String missingKey) {
    }

    private static final String DESK_KEY = "message.tech_revised.workstation.missing_desk";
    private static final String FRAME_KEY = "message.tech_revised.workstation.missing_frame";

    public static final List<Part> PARTS = List.of(
            // --- desk surface (y = 0): keyboard toward the chair, speakers behind ---
            new Part(-1, 0, 1, () -> ModBlocks.TESTED_SPEAKER.get(), "message.tech_revised.workstation.missing_speaker"),
            new Part(1, 0, 1, () -> ModBlocks.TESTED_SPEAKER.get(), "message.tech_revised.workstation.missing_speaker"),
            new Part(-1, 0, 0, () -> ModBlocks.TESTED_DESK.get(), DESK_KEY),
            new Part(1, 0, 0, () -> ModBlocks.TESTED_DESK.get(), DESK_KEY),
            new Part(0, 0, 1, () -> ModBlocks.TESTED_DESK.get(), DESK_KEY),
            new Part(0, 0, -1, () -> ModBlocks.TESTED_KEYBOARD.get(), "message.tech_revised.workstation.missing_keyboard"),
            new Part(-1, 0, -1, () -> ModBlocks.TESTED_MOUSE.get(), "message.tech_revised.workstation.missing_mouse"),
            new Part(1, 0, -1, () -> ModBlocks.TESTED_HDMI_CABLE.get(), "message.tech_revised.workstation.missing_hdmi"),
            // --- under-desk (y = -1): legs, tower under keyboard, power behind HDMI ---
            new Part(-1, -1, 1, () -> ModBlocks.TRANSPORT_FRAME.get(), FRAME_KEY),
            new Part(0, -1, 1, () -> ModBlocks.TRANSPORT_FRAME.get(), FRAME_KEY),
            new Part(1, -1, 1, () -> ModBlocks.TRANSPORT_FRAME.get(), FRAME_KEY),
            new Part(-1, -1, 0, () -> ModBlocks.TRANSPORT_FRAME.get(), FRAME_KEY),
            new Part(1, -1, 0, () -> ModBlocks.TRANSPORT_FRAME.get(), FRAME_KEY),
            new Part(-1, -1, -1, () -> ModBlocks.TRANSPORT_FRAME.get(), FRAME_KEY),
            new Part(1, -1, -1, () -> ModBlocks.TRANSPORT_FRAME.get(), FRAME_KEY),
            new Part(0, -1, -1, () -> ModBlocks.TESTED_DESKTOP_TOWER.get(), "message.tech_revised.workstation.missing_tower"),
            new Part(1, -1, 2, () -> ModBlocks.TESTED_POWER_CABLE.get(), "message.tech_revised.workstation.missing_power"),
            // --- chair in front of the desk ---
            new Part(0, 0, -2, () -> ModBlocks.TESTED_CHAIR_SEAT.get(), "message.tech_revised.workstation.missing_chair"),
            new Part(0, 1, -2, () -> ModBlocks.TESTED_CHAIR_BACKREST.get(), "message.tech_revised.workstation.missing_chair")
    );

    private WorkstationStructure() {
    }

    public static BlockPos localToWorld(BlockPos origin, Direction front, int localX, int localY, int localZ) {
        Direction right = front.getClockWise();
        Direction back = front.getOpposite();
        return origin.relative(right, localX).relative(back, localZ).offset(0, localY, 0);
    }

    public static Component validate(Level level, BlockPos controllerPos, Direction front) {
        for (Part part : PARTS) {
            BlockPos pos = localToWorld(controllerPos, front, part.x(), part.y(), part.z());
            if (!level.getBlockState(pos).is(part.block().get())) {
                return Component.translatable(part.missingKey());
            }
        }
        return null;
    }

    public static boolean isValid(Level level, BlockPos controllerPos, Direction front) {
        return validate(level, controllerPos, front) == null;
    }

    public static void updateFormedStates(Level level, BlockPos controllerPos, Direction front, boolean formed) {
        if (level.isClientSide()) {
            return;
        }
        for (Part part : PARTS) {
            Block block = part.block().get();
            if (block == ModBlocks.TESTED_CHAIR_SEAT.get()
                    || block == ModBlocks.TESTED_CHAIR_BACKREST.get()
                    || block == ModBlocks.TESTED_POWER_CABLE.get()) {
                continue;
            }
            BlockPos pos = localToWorld(controllerPos, front, part.x(), part.y(), part.z());
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(WorkstationKitBlock.FORMED) && state.getValue(WorkstationKitBlock.FORMED) != formed) {
                level.setBlock(pos, state.setValue(WorkstationKitBlock.FORMED, formed), Block.UPDATE_CLIENTS);
            }
        }
        BlockState controller = level.getBlockState(controllerPos);
        if (controller.hasProperty(WorkstationMonitorBlock.FORMED)
                && controller.getValue(WorkstationMonitorBlock.FORMED) != formed) {
            level.setBlock(controllerPos, controller.setValue(WorkstationMonitorBlock.FORMED, formed), Block.UPDATE_CLIENTS);
        }
    }
}
