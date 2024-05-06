package io.github.mortuusars.salt;

import io.github.mortuusars.salt.block.ISaltBlock;
import io.github.mortuusars.salt.configuration.ConfigGlobal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
public class Dissolving {

    /**
     * Performs necessary checks and if all passes - dissolves a block.
     * @return True if block was dissolved.
     */
    public static boolean maybeDissolveInRain(BlockState dissolvedState, ServerLevel level, BlockPos pos) {
        if (ConfigGlobal.instance.DISSOLVING_ENABLED
                && ConfigGlobal.instance.DISSOLVING_IN_RAIN
                && level.isRainingAt(pos.above())
                && level.random.nextDouble() < ConfigGlobal.instance.DISSOLVING_IN_RAIN_CHANCE) {
            dissolve(dissolvedState, level, pos, Fluids.EMPTY, false);
            return true;
        }
        return false;
    }

    /**
     * Performs necessary checks and if all passes - dissolves a block.
     * @return True if block was dissolved.
     */
    public static boolean maybeDissolve(BlockState dissolvedState, BlockPos dissolvedPos, BlockState adjacentState, BlockPos adjacentPos, ServerLevel level) {
        if (ConfigGlobal.instance.DISSOLVING_ENABLED
                && adjacentState.is(Salt.BlockTags.SALT_DISSOLVABLES) && level.random.nextDouble() < ConfigGlobal.instance.DISSOLVING_CHANCE) {
            FluidState fluidState = adjacentState.getFluidState();
            if (dissolvedPos.getY() > adjacentPos.getY())
                fluidState = Fluids.EMPTY.defaultFluidState();
            dissolve(dissolvedState, level, dissolvedPos, fluidState.getType(), fluidState.isSource());
            return true;
        }
        return false;
    }

    public static void dissolve(BlockState state, ServerLevel level, BlockPos pos, Fluid fluid, boolean isSource) {
        BlockState dissolvedState = state.getBlock() instanceof ISaltBlock saltBlock ?
                saltBlock.getDissolvedState(state, level, pos, fluid) :
                Blocks.AIR.defaultBlockState();

        if (shouldPlaceFluidSource(level, pos, fluid, isSource)) {
            if (dissolvedState.isAir())
                dissolvedState = fluid.defaultFluidState().createLegacyBlock();
            else if (dissolvedState.getBlock() instanceof SimpleWaterloggedBlock waterloggedBlock
                        && waterloggedBlock.canPlaceLiquid(level, pos, dissolvedState, fluid))
                dissolvedState = dissolvedState.setValue(BlockStateProperties.WATERLOGGED, true);
        }

        level.setBlockAndUpdate(pos, dissolvedState);

        onBlockDissolved(level, pos);
    }

    private static boolean shouldPlaceFluidSource(ServerLevel level, BlockPos pos, Fluid fluid, boolean isSource) {
        return ConfigGlobal.instance.DISSOLVING_FLUID_SOURCE_CONVERSION && fluid == Fluids.WATER && isSource;
    }

    private static void onBlockDissolved(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        RandomSource random = level.getRandom();

        level.playSound(null, center.x, center.y, center.z, Salt.Sounds.SALT_DISSOLVE, SoundSource.BLOCKS, 1f, random.nextFloat() * 0.2f + 0.9f);

        for (int i = 0; i < 6; i++) {
            level.sendParticles(ParticleTypes.SPIT, center.x + random.nextGaussian() * 0.35f,
                    center.y + random.nextGaussian() * 0.35f, center.z + random.nextGaussian() * 0.35f,
                    1, 0f, 0f, 0f, 0f);
        }
    }
}
