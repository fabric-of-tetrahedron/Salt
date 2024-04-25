package io.github.mortuusars.salt;

import io.github.mortuusars.salt.configuration.Configuration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;

public class Salting {

    public record FoodValue(int nutrition, float saturationModifier) {
        @Override
        public String toString() {
            return "{Nutrition:" + nutrition + ",Saturation:" + saturationModifier + "}";
        }
    }

    private static final String SALTED_KEY = "Salted";

    public static boolean isSalted(ItemStack itemStack) {
        return itemStack.hasTag() && Objects.requireNonNull(itemStack.getTag()).contains(SALTED_KEY);
    }

    /**
     * Same ItemStack is returned.
     */
    public static ItemStack setSalted(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean(SALTED_KEY, true);
        return itemStack;
    }

    public static FoodValue getAdditionalFoodValue(ItemStack stack) {
        @Nullable FoodValue foodValue = Configuration.FOOD_VALUES.get(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem()))
                .toString());
        return foodValue != null ? foodValue : new FoodValue(Configuration.SALTING_ADDITIONAL_NUTRITION.get(),
                Configuration.SALTING_ADDITIONAL_SATURATION_MODIFIER.get().floatValue());
    }
}
