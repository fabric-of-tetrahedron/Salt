package io.github.mortuusars.salt.block;

import io.github.mortuusars.salt.Dissolving;
import io.github.mortuusars.salt.Melting;
import io.github.mortuusars.salt.Salt;
import io.github.mortuusars.salt.Salt.SaltBlocks;
import io.github.mortuusars.salt.configuration.ConfigGlobal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Contains all common functionality of salt blocks.
 */
@SuppressWarnings("unused")
public interface ISaltBlock {
    @NotNull
    BlockState getDissolvedState(BlockState state, ServerLevel level, BlockPos pos, Fluid fluid);

    /**
     * Performs generic salt things on random tick.
     *
     * @return false if block was removed (by dissolving or something else)
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean onSaltRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ArrayList<Direction> directions = Arrays.stream(Direction.values())
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(directions);

        for (Direction direction : directions) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (Melting.tryMeltFromBlock(adjacentPos, level))
                break; // Melting only one block per random tick

            if (direction != Direction.DOWN && Dissolving.maybeDissolve(state, pos, adjacentState, adjacentPos, level))
                return false;
        }

        if (Dissolving.maybeDissolveInRain(getDissolvedState(state, level, pos, Fluids.WATER), level, pos))
            return false;

        maybeGrowCluster(state, pos, level);

        return true;
    }

    default void onSaltAnimateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0 && level.isRainingAt(pos.above())) {
            Direction direction = Direction.getRandom(random);
            if (direction != Direction.UP) {
                BlockPos blockpos = pos.relative(direction);
                BlockState blockstate = level.getBlockState(blockpos);
                if (!state.canOcclude() || !blockstate.isFaceSturdy(level, blockpos, direction.getOpposite())) {
                    double d0 = direction.getStepX() == 0 ? random.nextDouble() : 0.5D + (double) direction.getStepX() * 0.65D;
                    double d1 = direction.getStepY() == 0 ? random.nextDouble() : 0.5D + (double) direction.getStepY() * 0.65D;
                    double d2 = direction.getStepZ() == 0 ? random.nextDouble() : 0.5D + (double) direction.getStepZ() * 0.65D;
                    level.addParticle(ParticleTypes.SPIT, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    /**
     * Handles Cluster 'life-cycle'. Performs necessary checks and if all passes grows (or, in certain conditions, destroys) a cluster.
     *
     * @param baseState Block that grows a cluster.
     */
    static void maybeGrowCluster(BlockState baseState, BlockPos basePos, ServerLevel level) {
        if (baseState.is(Salt.BlockTags.SALT_CLUSTER_GROWABLES)
                && ConfigGlobal.instance.SALT_CLUSTER_GROWING_ENABLED
                && level.random.nextFloat() < ConfigGlobal.instance.SALT_CLUSTER_GROWING_CHANCE) {
            BlockPos clusterPos = basePos.above();
            if (canGrowCluster(clusterPos, level)) {
                Fluid drippingFluid = getFluidDrippingOn(level, clusterPos);

                if (drippingFluid == Fluids.WATER)
                    growCluster(baseState, basePos, level);
                else if (drippingFluid != Fluids.EMPTY)
                    level.destroyBlock(clusterPos, false);
            }
        }
    }

    static Fluid getFluidDrippingOn(ServerLevel level, BlockPos pos) {
        BlockPos blockpos = PointedDripstoneBlock.findStalactiteTipAboveCauldron(level, pos);
        return blockpos == null ? Fluids.EMPTY : PointedDripstoneBlock.getCauldronFillFluidType(level, blockpos);
    }

    static boolean canGrowCluster(BlockPos clusterPos, ServerLevel level) {
        BlockState blockStateAbove = level.getBlockState(clusterPos);
        return blockStateAbove.isAir() || (blockStateAbove.getBlock() instanceof SaltClusterBlock && !blockStateAbove.is(SaltBlocks.SALT_CLUSTER));
    }

    static void growCluster(BlockState state, BlockPos basePos, ServerLevel level) {
        BlockPos clusterPos = basePos.above();
        BlockState prevState = level.getBlockState(clusterPos);
        BlockState clusterState;
        if (prevState.isAir())
            clusterState = SaltBlocks.SMALL_SALT_BUD.defaultBlockState();
        else if (prevState.is(SaltBlocks.SMALL_SALT_BUD))
            clusterState = SaltBlocks.MEDIUM_SALT_BUD.defaultBlockState();
        else if (prevState.is(SaltBlocks.MEDIUM_SALT_BUD))
            clusterState = SaltBlocks.LARGE_SALT_BUD.defaultBlockState();
        else
            clusterState = SaltBlocks.SALT_CLUSTER.defaultBlockState();

        level.setBlockAndUpdate(clusterPos, clusterState.setValue(SaltClusterBlock.FACING, Direction.UP));

        Random random = new Random();

        level.playSound(null, clusterPos, Salt.Sounds.SALT_CLUSTER_FALL, SoundSource.BLOCKS, 0.3f, random.nextFloat() * 0.3f + 0.75f);

        BlockState placedClusterState = level.getBlockState(clusterPos);
        BlockParticleOption particleType = new BlockParticleOption(ParticleTypes.BLOCK, placedClusterState);
        VoxelShape shape = placedClusterState.getShape(level, clusterPos);
        for (int i = 0; i < 8; i++) {
            level.sendParticles(particleType,
                    clusterPos.getX() + random.nextDouble(shape.min(Direction.Axis.X), shape.max(Direction.Axis.X)),
                    clusterPos.getY() + random.nextDouble(shape.min(Direction.Axis.Y), shape.max(Direction.Axis.Y)),
                    clusterPos.getZ() + random.nextDouble(shape.min(Direction.Axis.Z), shape.max(Direction.Axis.Z)),
                    1, 0f, 0f, 0f, 0f);
        }
    }
}
