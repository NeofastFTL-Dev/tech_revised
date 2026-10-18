package com.neofast.tech_revised.integration.jade;

import com.neofast.tech_revised.block.custom.SolidFuelGeneratorBlock;
import com.neofast.tech_revised.block.custom.WorkstationMonitorBlock;
import com.neofast.tech_revised.block.entity.custom.ArgonOxygenDecarburizationConverterControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.BlastFurnaceControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.CokeOvenControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.CrusherBlockEntity;
import com.neofast.tech_revised.block.entity.custom.DrillingPlatformControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceInputBusBlockEntity;
import com.neofast.tech_revised.block.entity.custom.GenericIndustrialMachineBlockEntity;
import com.neofast.tech_revised.block.entity.custom.MultiInputIndustrialMachineBlockEntity;
import com.neofast.tech_revised.block.entity.custom.OxygenConverterControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.SolidFuelGeneratorBlockEntity;
import com.neofast.tech_revised.item.ModItems;
import com.neofast.tech_revised.multiblock.MachineLayouts;
import com.neofast.tech_revised.TechRevised;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Locale;

/**
 * Collects tech_revised machine state on the server (progress, multiblock formed
 * state, energy, fuel, electrode wear) and renders it as Jade tooltip lines.
 *
 * <p>Data keys are prefixed with {@code tr_} to avoid collisions in Jade's shared
 * server-data tag. Energy is read through the FE capability so every tile in the
 * mod is covered without needing per-class getters.</p>
 */
public enum TechRevisedDataProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final String TR_FORMED = "tr_formed";
    private static final String TR_PROGRESS = "tr_progress";
    private static final String TR_PROGRESS_MAX = "tr_progress_max";
    private static final String TR_ENERGY = "tr_energy";
    private static final String TR_ENERGY_MAX = "tr_energy_max";
    private static final String TR_BURN_LEFT = "tr_burn_left";
    private static final String TR_BURN_MAX = "tr_burn_max";
    private static final String TR_ELECTRODE = "tr_electrode";
    private static final String TR_ELECTRODE_MAX = "tr_electrode_max";

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(TechRevised.MOD_ID, "jade_data");
    }

    // ---------------------------------------------------------------------
    // Server side
    // ---------------------------------------------------------------------

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (be == null) {
            return;
        }
        Level level = accessor.getLevel();
        BlockState state = accessor.getBlockState();

        // Energy via capability: covers machines, controllers, the hatch and the generator.
        be.getCapability(ForgeCapabilities.ENERGY, null).ifPresent(energy -> {
            data.putInt(TR_ENERGY, energy.getEnergyStored());
            data.putInt(TR_ENERGY_MAX, energy.getMaxEnergyStored());
        });

        if (be instanceof CrusherBlockEntity crusher) {
            putProgress(data, crusher.getProgress(), crusher.getProcessTicks());
            data.putBoolean(TR_FORMED, MachineLayouts.isFormed(level, be.getBlockPos(), state));
        } else if (be instanceof GenericIndustrialMachineBlockEntity machine) {
            putProgress(data, machine.getProgress(), machine.getProcessTicks());
            data.putBoolean(TR_FORMED, MachineLayouts.isFormed(level, be.getBlockPos(), state));
        } else if (be instanceof MultiInputIndustrialMachineBlockEntity machine) {
            putProgress(data, machine.getProgress(), machine.getProcessTicks());
            data.putBoolean(TR_FORMED, MachineLayouts.isFormed(level, be.getBlockPos(), state));
        } else if (be instanceof ElectricArcFurnaceControllerBlockEntity eaf) {
            putProgress(data, eaf.getProgress(), eaf.getProcessTicks());
            data.putBoolean(TR_FORMED, eaf.isStructureFormed());
        } else if (be instanceof BlastFurnaceControllerBlockEntity blastFurnace) {
            putProgress(data, blastFurnace.getProgress(), blastFurnace.getProcessTicks());
            data.putBoolean(TR_FORMED, blastFurnace.isStructureFormed());
        } else if (be instanceof CokeOvenControllerBlockEntity cokeOven) {
            putProgress(data, cokeOven.getProgress(), cokeOven.getProcessTicks());
            data.putBoolean(TR_FORMED, cokeOven.isStructureFormed());
        } else if (be instanceof ArgonOxygenDecarburizationConverterControllerBlockEntity aod) {
            putProgress(data, aod.getProgress(), aod.getProcessTicks());
            data.putBoolean(TR_FORMED, aod.isStructureFormed());
        } else if (be instanceof OxygenConverterControllerBlockEntity oxygenConverter) {
            putProgress(data, oxygenConverter.getProgress(), oxygenConverter.getProcessTicks());
            data.putBoolean(TR_FORMED, oxygenConverter.isStructureFormed());
        } else if (be instanceof DrillingPlatformControllerBlockEntity drill) {
            putProgress(data, drill.getProgress(), drill.getProcessTicks());
            data.putBoolean(TR_FORMED, drill.isStructureFormed());
        } else if (be instanceof SolidFuelGeneratorBlockEntity generator) {
            data.putInt(TR_BURN_LEFT, generator.getBurnTimeLeft());
            data.putInt(TR_BURN_MAX, generator.getBurnDuration());
        }

        if (be instanceof ElectricArcFurnaceInputBusBlockEntity) {
            putElectrode(data, be);
        }
    }

    private static void putProgress(CompoundTag data, int progress, int processTicks) {
        data.putInt(TR_PROGRESS, progress);
        data.putInt(TR_PROGRESS_MAX, processTicks);
    }

    private static void putElectrode(CompoundTag data, BlockEntity be) {
        be.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.is(ModItems.ELECTRODE.get()) && stack.isDamageableItem()) {
                    data.putInt(TR_ELECTRODE, stack.getMaxDamage() - stack.getDamageValue());
                    data.putInt(TR_ELECTRODE_MAX, stack.getMaxDamage());
                    return;
                }
            }
        });
    }

    // ---------------------------------------------------------------------
    // Client side
    // ---------------------------------------------------------------------

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();

        appendVmState(tooltip, accessor.getBlockState());

        if (data.contains(TR_FORMED)) {
            boolean formed = data.getBoolean(TR_FORMED);
            tooltip.add(Component.translatable(formed ? "jade.tech_revised.formed" : "jade.tech_revised.incomplete")
                    .withStyle(formed ? ChatFormatting.GREEN : ChatFormatting.RED));
        }

        if (data.contains(TR_ELECTRODE)) {
            tooltip.add(Component.translatable("jade.tech_revised.electrode",
                    num(data.getInt(TR_ELECTRODE)), num(data.getInt(TR_ELECTRODE_MAX))));
        }

        if (data.contains(TR_PROGRESS)) {
            int progress = data.getInt(TR_PROGRESS);
            int max = data.getInt(TR_PROGRESS_MAX);
            int percent = max > 0 ? progress * 100 / max : 0;
            tooltip.add(Component.translatable("jade.tech_revised.progress", percent, num(progress), num(max)));
        }

        if (data.contains(TR_BURN_LEFT)) {
            int burnLeft = data.getInt(TR_BURN_LEFT);
            int burnMax = data.getInt(TR_BURN_MAX);
            if (burnMax > 0 && burnLeft > 0) {
                tooltip.add(Component.translatable("jade.tech_revised.fuel",
                        String.format(Locale.US, "%.1f", burnLeft / 20.0)));
            }
        }

        if (data.contains(TR_ENERGY)) {
            int stored = data.getInt(TR_ENERGY);
            int max = data.getInt(TR_ENERGY_MAX);
            if (max > 0) {
                tooltip.add(Component.translatable("jade.tech_revised.energy.with_max", num(stored), num(max))
                        .withStyle(ChatFormatting.GOLD));
            } else if (stored != 0) {
                tooltip.add(Component.translatable("jade.tech_revised.energy", num(stored))
                        .withStyle(ChatFormatting.GOLD));
            }
        }
    }

    /** Monitor running state lives in the blockstate, so no server data is needed. */
    private static void appendVmState(ITooltip tooltip, BlockState state) {
        BooleanProperty active = null;
        if (state.getBlock() instanceof WorkstationMonitorBlock) {
            active = WorkstationMonitorBlock.ACTIVE;
        } else if (state.getBlock() instanceof SolidFuelGeneratorBlock) {
            active = SolidFuelGeneratorBlock.LIT;
        }
        if (active == null) {
            return;
        }
        boolean on = state.getValue(active);
        String key;
        if (state.getBlock() instanceof SolidFuelGeneratorBlock) {
            key = on ? "jade.tech_revised.generator.burning" : "jade.tech_revised.generator.idle";
        } else {
            key = on ? "jade.tech_revised.vm.running" : "jade.tech_revised.vm.off";
        }
        tooltip.add(Component.translatable(key).withStyle(on ? ChatFormatting.GREEN : ChatFormatting.GRAY));
    }

    private static String num(int value) {
        return String.format(Locale.US, "%,d", value);
    }
}
