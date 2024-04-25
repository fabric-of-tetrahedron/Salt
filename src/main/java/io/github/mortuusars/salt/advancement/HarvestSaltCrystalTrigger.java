package io.github.mortuusars.salt.advancement;

import com.google.gson.JsonObject;
import io.github.mortuusars.salt.Salt;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class HarvestSaltCrystalTrigger extends SimpleCriterionTrigger<HarvestSaltCrystalTrigger.TriggerInstance> {
    private static final ResourceLocation ID = Salt.resource("harvest_salt_crystal");

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    @Override
    protected HarvestSaltCrystalTrigger.@NotNull TriggerInstance createInstance(@NotNull JsonObject json, ContextAwarePredicate predicate, @NotNull DeserializationContext conditionsParser) {
        return new HarvestSaltCrystalTrigger.TriggerInstance(predicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, triggerInstance -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(ContextAwarePredicate predicate) {
            super(HarvestSaltCrystalTrigger.ID, predicate);
        }
    }
}