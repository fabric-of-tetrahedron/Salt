package io.github.mortuusars.salt.integration.jei.category;

import io.github.mortuusars.salt.Salt;
import io.github.mortuusars.salt.Salt.SaltItems;
import io.github.mortuusars.salt.client.LangKeys;
import io.github.mortuusars.salt.integration.jei.SaltJeiPlugin;
import io.github.mortuusars.salt.integration.jei.resource.SaltCrystalGrowingDummy;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SaltCrystalGrowingCategory implements IRecipeCategory<SaltCrystalGrowingDummy> {
    public static final ResourceLocation UID = Salt.resource("salt_crystal_growing");
    private static final ResourceLocation TEXTURE = Salt.resource("textures/gui/jei/salt_crystal_growing.png");
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;

    public SaltCrystalGrowingCategory(IGuiHelper guiHelper) {
        title = Salt.translate(LangKeys.JEI_CATEGORY_SALT_CRYSTAL_GROWING);
        background = guiHelper.createDrawable(TEXTURE, 0, 0, 168, 152);
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(SaltItems.SALT_CLUSTER));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, @NotNull SaltCrystalGrowingDummy recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 104, 8)
                .addItemStack(new ItemStack(Items.WATER_BUCKET));

        builder.addSlot(RecipeIngredientRole.INPUT, 104, 37)
                .addItemStack(new ItemStack(Items.DRIPSTONE_BLOCK));

        builder.addSlot(RecipeIngredientRole.INPUT, 104, 66)
                .addItemStack(new ItemStack(Items.POINTED_DRIPSTONE));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 98)
                .addItemStacks(List.of(
                        new ItemStack(SaltItems.SMALL_SALT_BUD),
                        new ItemStack(SaltItems.MEDIUM_SALT_BUD),
                        new ItemStack(SaltItems.LARGE_SALT_BUD),
                        new ItemStack(SaltItems.SALT_CLUSTER)));

        builder.addSlot(RecipeIngredientRole.INPUT, 104, 127)
                .addItemStack(new ItemStack(SaltItems.RAW_ROCK_SALT_BLOCK));
    }

    @Override
    public @NotNull RecipeType<SaltCrystalGrowingDummy> getRecipeType() {
        return SaltJeiPlugin.SALT_CRYSTAL_GROWING_RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return title;
    }
    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }
    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }
}
