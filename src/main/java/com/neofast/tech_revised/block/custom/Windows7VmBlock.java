package com.neofast.tech_revised.block.custom;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.entity.custom.Windows7VmBlockEntity;
import com.neofast.tech_revised.integration.vm.VmCommandIntegration;
import com.neofast.tech_revised.item.ModItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class Windows7VmBlock extends ModBaseEntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public Windows7VmBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ACTIVE, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return net.minecraft.world.ItemInteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof Windows7VmBlockEntity vmBlockEntity)) {
            return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!serverPlayer.hasPermissions(2)) {
            serverPlayer.displayClientMessage(Component.translatable("message.tech_revised.vm.permission_denied"), true);
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }

        ItemStack heldStack = player.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND
                && !heldStack.is(ModItems.CONFIGURATOR.get())
                && player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.CONFIGURATOR.get())) {
            return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (heldStack.is(ModItems.CONFIGURATOR.get())) {
            NetworkHooks.openScreen(serverPlayer, vmBlockEntity, buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeUtf(vmBlockEntity.getVmName(), Windows7VmBlockEntity.MAX_VM_NAME_LENGTH);
                buffer.writeUtf(vmBlockEntity.getStartCommand(), Windows7VmBlockEntity.MAX_COMMAND_LENGTH);
                buffer.writeUtf(vmBlockEntity.getStopCommand(), Windows7VmBlockEntity.MAX_COMMAND_LENGTH);
            });
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }

        VmCommandIntegration.VmAction action = player.isShiftKeyDown()
                ? VmCommandIntegration.VmAction.STOP
                : VmCommandIntegration.VmAction.START;
        String vmName = VmCommandIntegration.resolveVmName(vmBlockEntity.getVmName());

        serverPlayer.displayClientMessage(
                VmCommandIntegration.getActionStartedMessage(action, vmName),
                true
        );
        VmCommandIntegration.executeAction(
                serverLevel,
                pos,
                serverPlayer,
                action,
                vmBlockEntity.getVmName(),
                vmBlockEntity.getStartCommand(),
                vmBlockEntity.getStopCommand()
        );

        if (action == VmCommandIntegration.VmAction.START) {
            grantAdvancement(serverPlayer, "vm_booted", "booted");
        }

        return net.minecraft.world.ItemInteractionResult.CONSUME;
    }

    private static void grantAdvancement(ServerPlayer player, String advancementId, String criterion) {
        net.minecraft.advancements.AdvancementHolder advancement = player.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(TechRevised.MOD_ID, advancementId));
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (!progress.isDone()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Windows7VmBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, FACING);
    }
}
