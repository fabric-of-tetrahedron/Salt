package io.github.mortuusars.salt.integration;

import io.github.mortuusars.salt.Salting;
import net.minecraft.world.item.ItemStack;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;

public class AppleSkinHandler implements AppleSkinApi {

    @Override
    public void registerEvents() {

        FoodValuesEvent.EVENT.register(foodValuesEvent -> {
            ItemStack foodStack = foodValuesEvent.itemStack;
            FoodValues modifiedFoodValues = foodValuesEvent.modifiedFoodValues;

            if (!Salting.isSalted(foodStack))
                return;

            Salting.FoodValue additionalFoodValue = Salting.getAdditionalFoodValue(foodStack);

            foodValuesEvent.modifiedFoodValues = new FoodValues(modifiedFoodValues.hunger + additionalFoodValue.nutrition(),
                    modifiedFoodValues.saturationModifier + additionalFoodValue.saturationModifier());
        });
    }
}
