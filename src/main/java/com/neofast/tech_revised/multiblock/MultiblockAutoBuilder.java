package com.neofast.tech_revised.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MultiblockAutoBuilder {
    public record Placement(BlockPos pos, Block block) {
    }

    private MultiblockAutoBuilder() {
    }

    public static boolean isCreativeBuild(Player player) {
        return player.isCreative() || player.getAbilities().instabuild;
    }

    public static void openScreen(Player player, BlockEntity entity) {
        if (player instanceof ServerPlayer serverPlayer && entity instanceof MenuProvider provider) {
            NetworkHooks.openScreen(serverPlayer, provider, entity.getBlockPos());
        }
    }

    public static boolean tryBuild(Level level, Player player, List<Placement> placements) {
        boolean creative = isCreativeBuild(player);
        Map<Item, Integer> requiredItems = new LinkedHashMap<>();
        List<Placement> toPlace = new ArrayList<>();

        for (Placement placement : placements) {
            BlockState currentState = level.getBlockState(placement.pos());
            if (currentState.is(placement.block())) {
                continue;
            }
            if (!currentState.canBeReplaced() && !creative) {
                player.displayClientMessage(Component.translatable(
                        "message.tech_revised.multiblock.autoconfig_blocked",
                        placement.pos().getX(), placement.pos().getY(), placement.pos().getZ()), true);
                return false;
            }

            toPlace.add(placement);
            Item item = placement.block().asItem();
            if (item != Items.AIR) {
                requiredItems.merge(item, 1, Integer::sum);
            }
        }

        if (!creative) {
            for (Map.Entry<Item, Integer> entry : requiredItems.entrySet()) {
                int available = countItemInInventory(player, entry.getKey());
                if (available < entry.getValue()) {
                    player.displayClientMessage(Component.translatable(
                            "message.tech_revised.multiblock.autoconfig_missing",
                            entry.getValue() - available,
                            entry.getKey().getDescription()), true);
                    return false;
                }
            }

            for (Map.Entry<Item, Integer> entry : requiredItems.entrySet()) {
                consumeItemFromInventory(player, entry.getKey(), entry.getValue());
            }
        }

        for (Placement placement : toPlace) {
            level.setBlock(placement.pos(), placement.block().defaultBlockState(), Block.UPDATE_ALL);
        }
        return true;
    }

    private static int countItemInInventory(Player player, Item item) {
        int count = 0;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void consumeItemFromInventory(Player player, Item item, int amount) {
        Inventory inventory = player.getInventory();
        int remaining = amount;
        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.is(item)) {
                continue;
            }

            int extracted = Math.min(remaining, stack.getCount());
            stack.shrink(extracted);
            remaining -= extracted;
            if (stack.isEmpty()) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        inventory.setChanged();
    }
}
