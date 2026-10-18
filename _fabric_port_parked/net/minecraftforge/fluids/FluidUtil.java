package net.minecraftforge.fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
public class FluidUtil {
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos, Direction side) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return false;
        IFluidHandler handler = ForgeCapabilities.get(be, ForgeCapabilities.FLUID_HANDLER, side).orElse(null);
        if (handler == null) {
            handler = ForgeCapabilities.get(be, ForgeCapabilities.FLUID_HANDLER, null).orElse(null);
        }
        if (handler == null) return false;
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof BucketItem bucket) {
            Fluid fluid = Fluids.EMPTY;
            for (Fluid candidate : net.minecraft.core.registries.BuiltInRegistries.FLUID) {
                if (candidate.getBucket() == bucket && candidate.getBucket() != Items.AIR) {
                    fluid = candidate;
                    break;
                }
            }
            if (fluid != Fluids.EMPTY) {
                int filled = handler.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.SIMULATE);
                if (filled >= 1000) {
                    handler.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.EXECUTE);
                    if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    return true;
                }
            } else {
                FluidStack drained = handler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                if (!drained.isEmpty() && drained.getAmount() >= 1000) {
                    handler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    if (!player.getAbilities().instabuild) {
                        player.setItemInHand(hand, new ItemStack(drained.getFluid().getBucket()));
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
