package net.minecraftforge.fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import java.util.function.Supplier;
public abstract class ForgeFlowingFluid extends FlowingFluid {
    private final Properties properties;
    protected ForgeFlowingFluid(Properties properties) { this.properties = properties; }
    @Override public Fluid getFlowing() { return properties.flowing.get(); }
    @Override public Fluid getSource() { return properties.source.get(); }
    @Override protected boolean canConvertToSource(net.minecraft.world.level.Level level) { return false; }
    @Override protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockEntity);
    }
    @Override protected int getSlopeFindDistance(LevelReader level) { return properties.slopeFindDistance; }
    @Override protected int getDropOff(LevelReader level) { return properties.levelDecreasePerBlock; }
    @Override public Item getBucket() { return properties.bucket == null ? Items.AIR : properties.bucket.get(); }
    @Override public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.is(FluidTags.WATER);
    }
    @Override public int getTickDelay(LevelReader level) { return properties.tickRate; }
    @Override protected float getExplosionResistance() { return 100.0F; }
    @Override protected BlockState createLegacyBlock(FluidState state) {
        if (properties.block == null) return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        return properties.block.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }
    @Override public boolean isSame(Fluid fluid) {
        return fluid == properties.source.get() || fluid == properties.flowing.get();
    }
    public static class Flowing extends ForgeFlowingFluid {
        public Flowing(Properties properties) { super(properties); }
        @Override protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
        @Override public int getAmount(FluidState state) { return state.getValue(LEVEL); }
        @Override public boolean isSource(FluidState state) { return false; }
    }
    public static class Source extends ForgeFlowingFluid {
        public Source(Properties properties) { super(properties); }
        @Override public int getAmount(FluidState state) { return 8; }
        @Override public boolean isSource(FluidState state) { return true; }
    }
    public static class Properties {
        private final Supplier<? extends FluidType> type;
        private final Supplier<? extends Fluid> source;
        private final Supplier<? extends Fluid> flowing;
        private Supplier<? extends Item> bucket;
        private Supplier<? extends LiquidBlock> block;
        private int slopeFindDistance = 4;
        private int levelDecreasePerBlock = 1;
        private int tickRate = 5;
        public Properties(Supplier<? extends FluidType> type, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
            this.type = type;
            this.source = still;
            this.flowing = flowing;
        }
        public Properties bucket(Supplier<? extends Item> bucket) { this.bucket = bucket; return this; }
        public Properties block(Supplier<? extends LiquidBlock> block) { this.block = block; return this; }
        public Properties slopeFindDistance(int slopeFindDistance) { this.slopeFindDistance = slopeFindDistance; return this; }
        public Properties levelDecreasePerBlock(int levelDecreasePerBlock) { this.levelDecreasePerBlock = levelDecreasePerBlock; return this; }
        public Properties tickRate(int tickRate) { this.tickRate = tickRate; return this; }
        public FluidType fluidType() { return type.get(); }
    }
}
