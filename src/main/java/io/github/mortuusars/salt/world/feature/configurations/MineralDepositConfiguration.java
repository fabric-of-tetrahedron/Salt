package io.github.mortuusars.salt.world.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.salt.configuration.ConfigGlobal;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.List;

public record MineralDepositConfiguration(List<DepositBlockStateInfo> mainStateInfos,
                                          DepositBlockStateInfo clusterStateInfo) implements FeatureConfiguration {
    public static final Codec<MineralDepositConfiguration> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                            Codec.list(DepositBlockStateInfo.CODEC)
                                    .fieldOf("mainTargets")
                                    .forGetter(config -> config.mainStateInfos),
                            DepositBlockStateInfo.CODEC
                                    .fieldOf("clusterTargets")
                                    .forGetter(config -> config.clusterStateInfo))
                    .apply(instance, MineralDepositConfiguration::new));

    public static DepositBlockStateInfo blockStateInfo(BlockStateProvider blockStateProvider, RuleTest ruleTest) {
        return new DepositBlockStateInfo(blockStateProvider, ruleTest);
    }

    public int getSize() {
        return ConfigGlobal.instance.ROCK_SALT_SIZE;
    }

    public float getClusterChance() {
        return (float) ConfigGlobal.instance.ROCK_SALT_CLUSTER_CHANCE;
    }

    public static class DepositBlockStateInfo {
        public static final Codec<DepositBlockStateInfo> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                                BlockStateProvider.CODEC.fieldOf("blockStateProvider")
                                        .forGetter(target -> target.blockStateProvider),
                                RuleTest.CODEC.fieldOf("ruleTest")
                                        .forGetter(target -> target.ruleTest))
                        .apply(instance, DepositBlockStateInfo::new));

        public final BlockStateProvider blockStateProvider;
        public final RuleTest ruleTest;

        DepositBlockStateInfo(BlockStateProvider blockStateProvider, RuleTest ruleTest) {
            this.ruleTest = ruleTest;
            this.blockStateProvider = blockStateProvider;
        }
    }
}
