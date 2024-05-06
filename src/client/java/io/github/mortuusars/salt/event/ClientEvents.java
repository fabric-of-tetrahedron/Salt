package io.github.mortuusars.salt.event;

import io.github.mortuusars.salt.Salt;
import io.github.mortuusars.salt.Salt.SaltItems;
import io.github.mortuusars.salt.Salting;
import io.github.mortuusars.salt.client.LangKeys;
import io.github.mortuusars.salt.integration.AppleSkinHandler;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ClientEvents {

    public static void registerTab() {

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(content -> {
            content.accept(SaltItems.SALT);
            content.accept(SaltItems.RAW_ROCK_SALT);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(content -> {
            content.accept(SaltItems.SALT_BLOCK);
            content.accept(SaltItems.ROCK_SALT_ORE);
            content.accept(SaltItems.DEEPSLATE_ROCK_SALT_ORE);
            content.accept(SaltItems.RAW_ROCK_SALT_BLOCK);
            content.accept(SaltItems.DEEPSLATE_ROCK_SALT_ORE);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(content -> {
            content.accept(SaltItems.SMALL_SALT_BUD);
            content.accept(SaltItems.MEDIUM_SALT_BUD);
            content.accept(SaltItems.LARGE_SALT_BUD);
            content.accept(SaltItems.SALT_CLUSTER);
            content.accept(SaltItems.SALT_LAMP);
        });
    }

    public static void registerAppleSkin() {
//        if (FabricLoader.getInstance().isModLoaded("appleskin")) {
//        }
    }

    //
//    @SubscribeEvent
    public static void registerModels() {
//        event.register(Salt.resource("item/salted_overlay"));
//        Minecraft.getInstance().getModelManager().getModel(Salt.resource("item/salted_overlay"));

        ModelLoadingPlugin.register(context -> {
            context.addModels(Salt.resource("item/salted_overlay"));
        });
    }

    //
    public static void onItemTooltipEvent(ItemStack itemStack, List<Component> toolTip) {
//        ItemStack itemStack = event.getItemStack();
        if (Salting.isSalted(itemStack)) {
//            List<Component> toolTip = event.getToolTip();
            Salting.FoodValue additionalFoodValue = Salting.getAdditionalFoodValue(itemStack);
            toolTip.add(toolTip.size() >= 1 ? 1 : 0, SaltedTooltip.get(additionalFoodValue.nutrition(),
                    additionalFoodValue.saturationModifier(), Screen.hasShiftDown() && !FabricLoader.getInstance().isModLoaded("appleskin")));
        }
    }

    public static class SaltedTooltip {
        public static final Style SALTED_STYLE = Style.EMPTY.withColor(0xF0D8D5);
        public static final Style SALTED_EXPANDED_PART_STYLE = Style.EMPTY.withColor(0xC7B7B5);

        public static MutableComponent get(int nutrition, float saturationModifier, boolean isExpanded) {
            MutableComponent base = Component.translatable(LangKeys.GUI_TOOLTIP_SALTED).withStyle(SALTED_STYLE);
            return isExpanded ? base.append(Component.translatable(LangKeys.GUI_TOOLTIP_SALTED_EXPANDED_PART,
                            nutrition > 0 ? "+" + nutrition : "-" + nutrition,
                            saturationModifier > 0 ? "+" + saturationModifier : "-" + saturationModifier)
                    .withStyle(SALTED_EXPANDED_PART_STYLE))
                    : base;
        }
    }
}
