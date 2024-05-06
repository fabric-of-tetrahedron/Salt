package io.github.mortuusars.salt.integration.jei;

import com.google.common.collect.ImmutableList;
import io.github.mortuusars.salt.Salt;
import io.github.mortuusars.salt.Salt.BlockTags;
import io.github.mortuusars.salt.Salt.SaltItems;
import io.github.mortuusars.salt.configuration.ConfigGlobal;
import io.github.mortuusars.salt.crafting.recipe.SaltingRecipe;
import io.github.mortuusars.salt.integration.jei.category.SaltCrystalGrowingCategory;
import io.github.mortuusars.salt.integration.jei.category.SaltEvaporationCategory;
import io.github.mortuusars.salt.integration.jei.resource.SaltCrystalGrowingDummy;
import io.github.mortuusars.salt.integration.jei.resource.SaltEvaporationDummy;
import io.github.mortuusars.salt.integration.jei.resource.SaltingShapelessExtension;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@JeiPlugin
public class SaltJeiPlugin implements IModPlugin {
    public static final RecipeType<SaltEvaporationDummy> SALT_EVAPORATION_RECIPE_TYPE =
            RecipeType.create(Salt.ID, "salt_evaporation", SaltEvaporationDummy.class);
    public static final RecipeType<SaltCrystalGrowingDummy> SALT_CRYSTAL_GROWING_RECIPE_TYPE =
            RecipeType.create(Salt.ID, "salt_crystal_growing", SaltCrystalGrowingDummy.class);

    private static final ResourceLocation ID = Salt.resource("jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        if (isSaltEvaporationEnabled())
            registration.addRecipeCategories(new SaltEvaporationCategory(registration.getJeiHelpers().getGuiHelper()));

        if (isSaltCrystalGrowingEnabled())
            registration.addRecipeCategories(new SaltCrystalGrowingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (isSaltEvaporationEnabled())
            registration.addRecipeCatalyst(new ItemStack(Items.CAULDRON), SALT_EVAPORATION_RECIPE_TYPE);

        if (isSaltCrystalGrowingEnabled())
            registration.addRecipeCatalyst(new ItemStack(SaltItems.RAW_ROCK_SALT_BLOCK), SALT_CRYSTAL_GROWING_RECIPE_TYPE);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (isSaltEvaporationEnabled())
            registration.addRecipes(SALT_EVAPORATION_RECIPE_TYPE, ImmutableList.of(new SaltEvaporationDummy()));

        if (isSaltCrystalGrowingEnabled())
            registration.addRecipes(SALT_CRYSTAL_GROWING_RECIPE_TYPE, ImmutableList.of(new SaltCrystalGrowingDummy()));
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory()
                .addCategoryExtension(SaltingRecipe.class, SaltingShapelessExtension::new);
    }

    private boolean isSaltEvaporationEnabled() {
        return ConfigGlobal.instance.JEI_SALT_EVAPORATION_ENABLED
                && ConfigGlobal.instance.EVAPORATION_ENABLED
                && ConfigGlobal.instance.EVAPORATION_CHANCE > 0.0d
                && !Objects.requireNonNull(BuiltInRegistries.BLOCK).getTag(BlockTags.HEATERS).isEmpty();
    }

    private boolean isSaltCrystalGrowingEnabled() {
        return ConfigGlobal.instance.JEI_SALT_CRYSTAL_GROWING_ENABLED
                && ConfigGlobal.instance.SALT_CLUSTER_GROWING_ENABLED
                && ConfigGlobal.instance.SALT_CLUSTER_GROWING_CHANCE > 0.0d;
    }
}
