package io.github.mortuusars.salt.mixin;

import io.github.mortuusars.salt.Evaporation;
import io.github.mortuusars.salt.configuration.ConfigGlobal;
import io.github.mortuusars.salt.helper.Heater;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void randomTick(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource random, CallbackInfo ci) {
//        System.out.println(ConfigGlobal.instance.EVAPORATION_ENABLED + " " + state.is(Blocks.WATER_CAULDRON));
        if (ConfigGlobal.instance.EVAPORATION_ENABLED && state.is(Blocks.WATER_CAULDRON)) {
//            System.out.println("BlockBehaviourMixin.randomTick");
            if (state.getValue(LayeredCauldronBlock.LEVEL) > 0
                    && Heater.isHeatSource(serverLevel.getBlockState(pos.below()))
                    && random.nextDouble() < ConfigGlobal.instance.EVAPORATION_CHANCE) {
                Evaporation.evaporateWaterAndFormSalt(state, serverLevel, pos, random);
            }

            ci.cancel();
        }
    }
}
