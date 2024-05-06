package io.github.mortuusars.salt;

import io.github.mortuusars.salt.Salt.Sounds.Types;
import io.github.mortuusars.salt.advancement.HarvestSaltCrystalTrigger;
import io.github.mortuusars.salt.advancement.SaltEvaporationTrigger;
import io.github.mortuusars.salt.advancement.SaltedFoodConsumedTrigger;
import io.github.mortuusars.salt.block.SaltBlock;
import io.github.mortuusars.salt.block.SaltCauldronBlock;
import io.github.mortuusars.salt.block.SaltClusterBlock;
import io.github.mortuusars.salt.block.SaltSandBlock;
import io.github.mortuusars.salt.configuration.ConfigGlobal;
import io.github.mortuusars.salt.crafting.recipe.SaltingRecipe;
import io.github.mortuusars.salt.event.VillagerEvents;
import io.github.mortuusars.salt.item.SaltItem;
import io.github.mortuusars.salt.world.feature.MineralDepositFeature;
import io.github.mortuusars.salt.world.feature.configurations.MineralDepositConfiguration;
import io.github.mortuusars.salt.world.feature.configurations.MineralDepositConfiguration.DepositBlockStateInfo;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.material.MapColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

import static io.github.mortuusars.salt.configuration.ConfigGlobal.configHolder;

public class Salt implements ModInitializer {

    public static final String ID = "salt";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);


    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Salt for fabric!");

        configHolder = AutoConfig.register(ConfigGlobal.class, GsonConfigSerializer::new);
        configHolder.registerSaveListener((configHolder, configGlobal) -> {
            ConfigGlobal.instance = configGlobal;
            configGlobal.onConfigChanged();
            return InteractionResult.SUCCESS;
        });
        ConfigGlobal.instance.initConfig();

        Sounds.register();
        Types.register();

        SaltBlocks.register();
        SaltItems.register();
        SaltItemTags.register();
        BlockTags.register();
        WorldGenFeatures.register();
        Advancements.register();
        RecipeSerializers.register();

        VillagerEvents.registerVillagerTrades();

        // 'randomTick' is used in mixin to convert Water Cauldron to Salt Cauldron:
        Blocks.WATER_CAULDRON.isRandomlyTicking = true;

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (state.getBlock() instanceof SaltClusterBlock saltClusterBlock) {
                saltClusterBlock.onDestroyedByPlayer(world, player, pos, state, blockEntity);
            }
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack itemInHand = player.getItemInHand(hand);

            if (Salting.isSalted(itemInHand)) {
                if (player instanceof ServerPlayer serverPlayer
                        && !serverPlayer.isCreative()
                        && serverPlayer.canEat(false)) {
                    Advancements.SALTED_FOOD_CONSUMED.trigger(serverPlayer);
                }
            }

            return InteractionResultHolder.pass(itemInHand);
        });
    }

    public static MutableComponent translate(String key, Object... args) {
        return Component.translatable(key, args);
    }

    public static class SaltBlocks {
//        private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Salt.ID);

        public static void register() {
            SALT_BLOCK = Registry.register(BuiltInRegistries.BLOCK,
                    Salt.id("salt_block"), new SaltSandBlock(0xe7d5cf, BlockBehaviour.Properties.copy(Blocks.SAND)));
            ROCK_SALT_ORE = Registry.register(BuiltInRegistries.BLOCK,
                    Salt.id("rock_salt_ore"),
                    new SaltBlock(Blocks.STONE.defaultBlockState(), BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_GRAY)
                            .randomTicks()
                            .strength(2.5F)
                            .requiresCorrectToolForDrops()
                            .sound(Sounds.Types.SALT)));
            DEEPSLATE_ROCK_SALT_ORE = Registry.register(BuiltInRegistries.BLOCK,
                    Salt.id("deepslate_rock_salt_ore"),
                    new SaltBlock(Blocks.DEEPSLATE.defaultBlockState(), BlockBehaviour.Properties.copy(ROCK_SALT_ORE)
                            .mapColor(MapColor.COLOR_GRAY)));
            RAW_ROCK_SALT_BLOCK = Registry.register(BuiltInRegistries.BLOCK,
                    Salt.id("raw_rock_salt_block"),
                    new SaltBlock(BlockBehaviour.Properties.copy(ROCK_SALT_ORE)
                            .sound(Sounds.Types.SALT_CLUSTER)));
            SALT_CLUSTER = Registry.register(BuiltInRegistries.BLOCK,
                    Salt.id("salt_cluster"),
                    new SaltClusterBlock(7, 3, BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_GRAY)
                            .noOcclusion()
                            .randomTicks()
                            .strength(1.5F)
                            .sound(Sounds.Types.SALT_CLUSTER)
                            .lightLevel(state -> 3)
                            .dynamicShape()));
            MEDIUM_SALT_BUD = Registry.register(BuiltInRegistries.BLOCK,
                    Salt.id("medium_salt_bud"),
                    new SaltClusterBlock(4, 3, BlockBehaviour.Properties.copy(SALT_CLUSTER)
                            .lightLevel(state -> 2)
                            .sound(Sounds.Types.MEDIUM_SALT_BUD)));
            LARGE_SALT_BUD = Registry.register(BuiltInRegistries.BLOCK,
                    Salt.id("large_salt_bud"),
                    new SaltClusterBlock(5, 3, BlockBehaviour.Properties.copy(SALT_CLUSTER)
                            .lightLevel(state -> 2)
                            .sound(Sounds.Types.LARGE_SALT_BUD)));
            SMALL_SALT_BUD = Registry.register(BuiltInRegistries.BLOCK,
                    Salt.id("small_salt_bud"),
                    new SaltClusterBlock(3, 4, BlockBehaviour.Properties.copy(SALT_CLUSTER)
                            .lightLevel(state -> 1)
                            .sound(Sounds.Types.SMALL_SALT_BUD)));
            SALT_CAULDRON = Registry.register(BuiltInRegistries.BLOCK,
                    Salt.id("salt_cauldron"),
                    new SaltCauldronBlock(LayeredCauldronBlock.RAIN, CauldronInteraction.EMPTY));
            SALT_LAMP = Registry.register(BuiltInRegistries.BLOCK,
                    Salt.id("salt_lamp"),
                    new SaltBlock(Blocks.SPRUCE_SLAB.defaultBlockState(),
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_ORANGE)
                                    .sound(Sounds.Types.SALT)
                                    .randomTicks()
                                    .strength(2f)
                                    .lightLevel(blockState -> 15)));
        }

        public static SandBlock SALT_BLOCK;
        public static SaltBlock ROCK_SALT_ORE;
        public static SaltBlock DEEPSLATE_ROCK_SALT_ORE;
        public static SaltBlock RAW_ROCK_SALT_BLOCK;
        public static SaltClusterBlock SALT_CLUSTER;
        public static SaltClusterBlock LARGE_SALT_BUD;
        public static SaltClusterBlock MEDIUM_SALT_BUD;
        public static SaltClusterBlock SMALL_SALT_BUD;
        public static SaltCauldronBlock SALT_CAULDRON;
        public static SaltBlock SALT_LAMP;
    }

    public static class SaltItems {
//        private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Salt.ID);


        public static void register() {
            SALT = Registry.register(BuiltInRegistries.ITEM,
                    Salt.id("salt"), new SaltItem(new Item.Properties()));
            RAW_ROCK_SALT = Registry.register(BuiltInRegistries.ITEM,
                    Salt.id("raw_rock_salt"), new Item(new Item.Properties()));
            SALT_BLOCK = Registry.register(BuiltInRegistries.ITEM,
                    Salt.id("salt_block"), new BlockItem(SaltBlocks.SALT_BLOCK, new Item.Properties()));
            ROCK_SALT_ORE = Registry.register(BuiltInRegistries.ITEM,
                    Salt.id("rock_salt_ore"), new BlockItem(SaltBlocks.ROCK_SALT_ORE, new Item.Properties()));
            DEEPSLATE_ROCK_SALT_ORE = Registry.register(BuiltInRegistries.ITEM,
                    Salt.id("deepslate_rock_salt_ore"), new BlockItem(SaltBlocks.DEEPSLATE_ROCK_SALT_ORE, new Item.Properties()));
            RAW_ROCK_SALT_BLOCK = Registry.register(BuiltInRegistries.ITEM,
                    Salt.id("raw_rock_salt_block"), new BlockItem(SaltBlocks.RAW_ROCK_SALT_BLOCK, new Item.Properties()));
            SALT_CLUSTER = Registry.register(BuiltInRegistries.ITEM,
                    Salt.id("salt_cluster"), new BlockItem(SaltBlocks.SALT_CLUSTER, new Item.Properties()));
            LARGE_SALT_BUD = Registry.register(BuiltInRegistries.ITEM,
                    Salt.id("large_salt_bud"), new BlockItem(SaltBlocks.LARGE_SALT_BUD, new Item.Properties()));
            MEDIUM_SALT_BUD = Registry.register(BuiltInRegistries.ITEM,
                    Salt.id("medium_salt_bud"), new BlockItem(SaltBlocks.MEDIUM_SALT_BUD, new Item.Properties()));
            SMALL_SALT_BUD = Registry.register(BuiltInRegistries.ITEM,
                    Salt.id("small_salt_bud"), new BlockItem(SaltBlocks.SMALL_SALT_BUD, new Item.Properties()));

            SALT_LAMP = Registry.register(BuiltInRegistries.ITEM,
                    Salt.id("salt_lamp"), new BlockItem(SaltBlocks.SALT_LAMP, new Item.Properties()));
        }

        public static Item SALT;
        public static Item RAW_ROCK_SALT;
        public static BlockItem SALT_BLOCK;
        public static BlockItem ROCK_SALT_ORE;
        public static BlockItem DEEPSLATE_ROCK_SALT_ORE;
        public static BlockItem RAW_ROCK_SALT_BLOCK;
        public static BlockItem SALT_CLUSTER;
        public static BlockItem LARGE_SALT_BUD;
        public static BlockItem MEDIUM_SALT_BUD;
        public static BlockItem SMALL_SALT_BUD;
        public static BlockItem SALT_LAMP;
    }

    public static class Advancements {
        public static final SaltEvaporationTrigger SALT_EVAPORATED = new SaltEvaporationTrigger();
        public static final SaltedFoodConsumedTrigger SALTED_FOOD_CONSUMED = new SaltedFoodConsumedTrigger();
        public static final HarvestSaltCrystalTrigger HARVEST_SALT_CRYSTAL = new HarvestSaltCrystalTrigger();

        public static void register() {
            CriteriaTriggers.register(SALT_EVAPORATED);
            CriteriaTriggers.register(SALTED_FOOD_CONSUMED);
            CriteriaTriggers.register(HARVEST_SALT_CRYSTAL);
        }
    }

    public static class Sounds {
//        private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Salt.ID);

        public static SoundEvent SALT_DISSOLVE;
        public static SoundEvent MELT;
        public static SoundEvent CAULDRON_EVAPORATE;
        public static SoundEvent BUBBLE_POP;
        public static SoundEvent SALT_CAULDRON_REMOVE_SALT;

        public static SoundEvent SALT_BREAK;
        public static SoundEvent SALT_STEP;
        public static SoundEvent SALT_PLACE;
        public static SoundEvent SALT_HIT;
        public static SoundEvent SALT_FALL;

        public static SoundEvent SALT_CLUSTER_BREAK;
        public static SoundEvent SALT_CLUSTER_STEP;
        public static SoundEvent SALT_CLUSTER_PLACE;
        public static SoundEvent SALT_CLUSTER_HIT;
        public static SoundEvent SALT_CLUSTER_FALL;

        public static SoundEvent LARGE_SALT_BUD_BREAK;
        public static SoundEvent LARGE_SALT_BUD_STEP;
        public static SoundEvent LARGE_SALT_BUD_PLACE;
        public static SoundEvent LARGE_SALT_BUD_HIT;
        public static SoundEvent LARGE_SALT_BUD_FALL;

        public static SoundEvent MEDIUM_SALT_BUD_BREAK;
        public static SoundEvent MEDIUM_SALT_BUD_STEP;
        public static SoundEvent MEDIUM_SALT_BUD_PLACE;
        public static SoundEvent MEDIUM_SALT_BUD_HIT;
        public static SoundEvent MEDIUM_SALT_BUD_FALL;

        public static SoundEvent SMALL_SALT_BUD_BREAK;
        public static SoundEvent SMALL_SALT_BUD_STEP;
        public static SoundEvent SMALL_SALT_BUD_PLACE;
        public static SoundEvent SMALL_SALT_BUD_HIT;
        public static SoundEvent SMALL_SALT_BUD_FALL;

        public static void register() {
            SALT_DISSOLVE = register("salt.dissolve");
            MELT = register("melt");
            CAULDRON_EVAPORATE = register("cauldron.evaporate");
            BUBBLE_POP = register("bubble_pop");
            SALT_CAULDRON_REMOVE_SALT = register("salt_cauldron.remove_salt");

            SALT_BREAK = register("salt.break");
            SALT_STEP = register("salt.step");
            SALT_PLACE = register("salt.place");
            SALT_HIT = register("salt.hit");
            SALT_FALL = register("salt.fall");

            SALT_CLUSTER_BREAK = register("salt_cluster.break");
            SALT_CLUSTER_STEP = register("salt_cluster.step");
            SALT_CLUSTER_PLACE = register("salt_cluster.place");
            SALT_CLUSTER_HIT = register("salt_cluster.hit");
            SALT_CLUSTER_FALL = register("salt_cluster.fall");

            LARGE_SALT_BUD_BREAK = register("large_salt_bud.break");
            LARGE_SALT_BUD_STEP = register("large_salt_bud.step");
            LARGE_SALT_BUD_PLACE = register("large_salt_bud.place");
            LARGE_SALT_BUD_HIT = register("large_salt_bud.hit");
            LARGE_SALT_BUD_FALL = register("large_salt_bud.fall");

            MEDIUM_SALT_BUD_BREAK = register("medium_salt_bud.break");
            MEDIUM_SALT_BUD_STEP = register("medium_salt_bud.step");
            MEDIUM_SALT_BUD_PLACE = register("medium_salt_bud.place");
            MEDIUM_SALT_BUD_HIT = register("medium_salt_bud.hit");
            MEDIUM_SALT_BUD_FALL = register("medium_salt_bud.fall");

            SMALL_SALT_BUD_BREAK = register("small_salt_bud.break");
            SMALL_SALT_BUD_STEP = register("small_salt_bud.step");
            SMALL_SALT_BUD_PLACE = register("small_salt_bud.place");
            SMALL_SALT_BUD_HIT = register("small_salt_bud.hit");
            SMALL_SALT_BUD_FALL = register("small_salt_bud.fall");
        }

        public static SoundEvent register(String path) {
            ResourceLocation id = Salt.id(path);
            return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
        }

        public static class Types {

            public static SoundType SALT;
            public static SoundType SALT_CLUSTER;
            public static SoundType LARGE_SALT_BUD;
            public static SoundType MEDIUM_SALT_BUD;
            public static SoundType SMALL_SALT_BUD;

            public static void register() {
                SALT = new SoundType(1.0f, 1.0f,
                        SALT_BREAK,
                        SALT_STEP,
                        SALT_PLACE,
                        SALT_HIT,
                        SALT_FALL);
                SALT_CLUSTER = new SoundType(1.0f, 1.0f,
                        SALT_CLUSTER_BREAK,
                        SALT_CLUSTER_STEP,
                        SALT_CLUSTER_PLACE,
                        SALT_CLUSTER_HIT,
                        SALT_CLUSTER_FALL);
                LARGE_SALT_BUD = new SoundType(1.0f, 1.0f,
                        LARGE_SALT_BUD_BREAK,
                        LARGE_SALT_BUD_STEP,
                        LARGE_SALT_BUD_PLACE,
                        LARGE_SALT_BUD_HIT,
                        LARGE_SALT_BUD_FALL);
                MEDIUM_SALT_BUD = new SoundType(1.0f, 1.0f,
                        MEDIUM_SALT_BUD_BREAK,
                        MEDIUM_SALT_BUD_STEP,
                        MEDIUM_SALT_BUD_PLACE,
                        MEDIUM_SALT_BUD_HIT,
                        MEDIUM_SALT_BUD_FALL);
                SMALL_SALT_BUD = new SoundType(1.0f, 1.0f,
                        SMALL_SALT_BUD_BREAK,
                        SMALL_SALT_BUD_STEP,
                        SMALL_SALT_BUD_PLACE,
                        SMALL_SALT_BUD_HIT,
                        SMALL_SALT_BUD_FALL);
            }
        }
    }

    public static class SaltItemTags {

        public static TagKey<Item> CAN_BE_SALTED;
        public static TagKey<Item> FORGE_SALTS;
        public static TagKey<Item> FORGE_TORCHES;

        public static void register() {
            CAN_BE_SALTED = TagKey.create(Registries.ITEM,
                    Salt.resource("can_be_salted"));
            FORGE_SALTS = TagKey.create(Registries.ITEM,
                    new ResourceLocation("fabric", "salts"));
            FORGE_TORCHES = TagKey.create(Registries.ITEM,
                    new ResourceLocation("fabric:torches"));
        }
    }

    public static class BlockTags {

        public static TagKey<Block> HEATERS;
        public static TagKey<Block> SALT_CLUSTER_GROWABLES;
        public static TagKey<Block> SALT_DISSOLVABLES;
        public static TagKey<Block> MELTABLES;
        public static TagKey<Block> SALT_CLUSTER_REPLACEABLES;

        public static void register() {
            HEATERS = TagKey.create(Registries.BLOCK,
                    Salt.resource("heaters"));
            SALT_CLUSTER_GROWABLES = TagKey.create(Registries.BLOCK,
                    Salt.resource("salt_cluster_growables"));
            SALT_DISSOLVABLES = TagKey.create(Registries.BLOCK,
                    Salt.resource("salt_dissolvables"));
            MELTABLES = TagKey.create(Registries.BLOCK,
                    Salt.resource("meltables"));
            SALT_CLUSTER_REPLACEABLES = TagKey.create(Registries.BLOCK,
                    Salt.resource("salt_cluster_replaceables"));
        }
    }

    public static class BiomeTags {
        public static final TagKey<Biome> HAS_ROCK_SALT_DEPOSITS = TagKey.create(Registries.BIOME, Salt.resource("has_rock_salt_deposits"));
    }

    public static class EntityTypes {
    }

    public static class WorldGenFeatures {
        public static Feature<MineralDepositConfiguration> MINERAL_DEPOSIT;

        public static void register() {
            MINERAL_DEPOSIT = Registry.register(BuiltInRegistries.FEATURE, id("mineral_deposit"),
                    new MineralDepositFeature(MineralDepositConfiguration.CODEC));
        }
    }

    public static class PlacedFeatures {

        public static final ResourceKey<PlacedFeature> MINERAL_ROCK_SALT_PLACED_KEY = createKey("mineral_rock_salt");

        public static void bootstrap(BootstapContext<PlacedFeature> context) {
            HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

            register(context, MINERAL_ROCK_SALT_PLACED_KEY, configuredFeatures.getOrThrow(ConfiguredFeatures.MINERAL_ROCK_SALT),
                    CountPlacement.of(1),
                    RarityFilter.onAverageOnceEvery(8),
                    InSquarePlacement.spread(),
                    HeightRangePlacement.uniform(VerticalAnchor.absolute(-5), VerticalAnchor.absolute(110)),
                    BiomeFilter.biome());
        }


        @SuppressWarnings("SameParameterValue")
        private static ResourceKey<PlacedFeature> createKey(String name) {
            return ResourceKey.create(Registries.PLACED_FEATURE, Salt.resource(name));
        }

        private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                     List<PlacementModifier> modifiers) {
            context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
        }

        @SuppressWarnings("SameParameterValue")
        private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                     PlacementModifier... modifiers) {
            register(context, key, configuration, List.of(modifiers));
        }
    }

    public static class ConfiguredFeatures {

        public static final ResourceKey<ConfiguredFeature<?, ?>> MINERAL_ROCK_SALT = registerKey("mineral_rock_salt");

        private static final Supplier<DepositBlockStateInfo> ROCK_SALT_STONE_BLOCKS = () -> MineralDepositConfiguration.blockStateInfo(
                new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                        .add(SaltBlocks.ROCK_SALT_ORE.defaultBlockState(), 8)
                        .add(SaltBlocks.RAW_ROCK_SALT_BLOCK.defaultBlockState(), 1)
                        .build()),
                new TagMatchTest(net.minecraft.tags.BlockTags.STONE_ORE_REPLACEABLES));

        private static final Supplier<MineralDepositConfiguration.DepositBlockStateInfo> ROCK_SALT_DEEPSLATE_BLOCKS = () -> MineralDepositConfiguration.blockStateInfo(
                new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                        .add(SaltBlocks.DEEPSLATE_ROCK_SALT_ORE.defaultBlockState(), 8)
                        .add(SaltBlocks.RAW_ROCK_SALT_BLOCK.defaultBlockState(), 1)
                        .build()),
                new TagMatchTest(net.minecraft.tags.BlockTags.DEEPSLATE_ORE_REPLACEABLES));

        private static final Supplier<MineralDepositConfiguration.DepositBlockStateInfo> ROCK_SALT_CLUSTERS = () -> MineralDepositConfiguration.blockStateInfo(
                new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                        .add(SaltBlocks.SALT_CLUSTER.defaultBlockState(), 16)
                        .add(SaltBlocks.LARGE_SALT_BUD.defaultBlockState(), 2)
                        .add(SaltBlocks.MEDIUM_SALT_BUD.defaultBlockState(), 1)
                        .add(SaltBlocks.SMALL_SALT_BUD.defaultBlockState(), 1)
                        .build()),
                new TagMatchTest(BlockTags.SALT_CLUSTER_REPLACEABLES));

        public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
            register(context, MINERAL_ROCK_SALT, WorldGenFeatures.MINERAL_DEPOSIT, new MineralDepositConfiguration(
                    List.of(ROCK_SALT_STONE_BLOCKS.get(), ROCK_SALT_DEEPSLATE_BLOCKS.get()), ROCK_SALT_CLUSTERS.get()));
        }

        public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
            return ResourceKey.create(Registries.CONFIGURED_FEATURE, Salt.resource(name));
        }

        @SuppressWarnings("SameParameterValue")
        private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstapContext<ConfiguredFeature<?, ?>> context,
                                                                                              ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
            context.register(key, new ConfiguredFeature<>(feature, configuration));
        }
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(ID, path);
    }


    public static class RecipeSerializers {
//        private static final RecipeSerializer<?> RECIPE_SERIALIZERS =Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,Salt.id("salting"),new SaltingRecipe.Serializer());

        public static RecipeSerializer<?> SALTING;

        public static void register() {
            SALTING = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Salt.id("salting"), new SaltingRecipe.Serializer());
//            SALTING = RecipeSerializer.register("salting",
//                    new SaltingRecipe.Serializer());
        }
    }
}