package io.github.mortuusars.salt.data.provider;

import io.github.mortuusars.salt.Salt;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    public Recipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> recipeConsumer) {

        // RAW ROCK SALT
        ShapelessRecipeBuilder.shapeless(Salt.Items.RAW_ROCK_SALT.get(), 9)
                .requires(Salt.Items.RAW_ROCK_SALT_BLOCK.get())
                .unlockedBy("has_rock_salt", has(Salt.Items.RAW_ROCK_SALT.get()))
                .save(recipeConsumer);

        ShapedRecipeBuilder.shaped(Salt.Items.RAW_ROCK_SALT_BLOCK.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', Salt.Items.RAW_ROCK_SALT.get())
                .unlockedBy("has_rock_salt", has(Salt.Items.RAW_ROCK_SALT.get()))
                .save(recipeConsumer);

        // SALT
        ShapelessRecipeBuilder.shapeless(Salt.Items.SALT.get(), 9)
                .requires(Salt.Items.SALT_BLOCK.get())
                .unlockedBy("has_salt", has(Salt.Items.SALT.get()))
                .save(recipeConsumer, Salt.resource("salt_unpacking"));

        ShapedRecipeBuilder.shaped(Salt.Items.SALT_BLOCK.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', Salt.Items.SALT.get())
                .unlockedBy("has_salt", has(Salt.Items.SALT.get()))
                .save(recipeConsumer, Salt.resource("salt_packing"));

        ShapedRecipeBuilder.shaped(Salt.Items.SALT_LAMP.get())
                .pattern(" S ")
                .pattern(" T ")
                .pattern(" W ")
                .define('S', Salt.Items.RAW_ROCK_SALT_BLOCK.get())
                .define('T', Salt.ItemTags.FORGE_TORCHES)
                .define('W', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_rock_salt", has(Salt.Items.RAW_ROCK_SALT.get()))
                .save(recipeConsumer);

        ShapelessRecipeBuilder.shapeless(Salt.Items.SALT.get())
                .requires(Salt.Items.RAW_ROCK_SALT.get())
                .unlockedBy("has_rock_salt", has(Salt.Items.RAW_ROCK_SALT.get()))
                .save(recipeConsumer, Salt.resource("salt_from_raw_rock_salt"));

        ShapelessRecipeBuilder.shapeless(Items.GUNPOWDER, 2)
                .requires(Salt.ItemTags.FORGE_SALTS)
                .requires(Salt.ItemTags.FORGE_SALTS)
                .requires(ItemTags.COALS)
                .unlockedBy("has_salt", has(Salt.Items.SALT.get()))
                .save(recipeConsumer, Salt.resource("gunpowder"));
    }
}
