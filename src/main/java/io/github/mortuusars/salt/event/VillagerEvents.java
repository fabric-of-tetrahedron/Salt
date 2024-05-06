package io.github.mortuusars.salt.event;

import io.github.mortuusars.salt.Salt.SaltItems;
import io.github.mortuusars.salt.configuration.ConfigGlobal;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.EmeraldForItems;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.entity.npc.VillagerTrades.ItemsForEmeralds;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class VillagerEvents {
    public static void registerVillagerTrades() {
        if (!ConfigGlobal.instance.BUTCHER_SALT_TRADES_ENABLED)
            return;

        TradeOfferHelper.registerVillagerOffers(VillagerProfession.BUTCHER, 1, trade -> {
            trade.add(emeraldForItemsTrade(SaltItems.SALT, 18, 12, 2));
            trade.add(emeraldForItemsTrade(SaltItems.SALT, 8, 16, 2));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.BUTCHER, 3, trade -> {
            trade.add(itemForEmeraldTrade(SaltItems.SALT, 14, 5, 10, 6));
            trade.add(itemForEmeraldTrade(SaltItems.SALT, 8, 3, 12, 6));
        });
    }

    private static ItemListing emeraldForItemsTrade(ItemLike item, int count, int maxTrades, int xp) {
        return new EmeraldForItems(item, count, maxTrades, xp);
    }

    private static ItemListing itemForEmeraldTrade(ItemLike item, int itemsCount, int emeralds, int maxTrades, int xp) {
        return new ItemsForEmeralds(new ItemStack(item, itemsCount), emeralds, itemsCount, maxTrades, xp);
    }
}
