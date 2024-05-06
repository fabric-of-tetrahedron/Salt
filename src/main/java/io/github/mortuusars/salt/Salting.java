package io.github.mortuusars.salt;

import io.github.mortuusars.salt.configuration.ConfigGlobal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
        @Nullable FoodValue foodValue = (FoodValue) ConfigGlobal.instance.FOOD_VALUES.get(stack.getDescriptionId());
//        @Nullable FoodValue foodValue = Configuration.FOOD_VALUES.get(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem()))
//                .toString());
        return foodValue != null ? foodValue : new FoodValue(ConfigGlobal.instance.SALTING_ADDITIONAL_NUTRITION,
                (float) ConfigGlobal.instance.SALTING_ADDITIONAL_SATURATION_MODIFIER);
    }
}
