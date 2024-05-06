package io.github.mortuusars.salt;

import io.github.mortuusars.salt.Salt.SaltBlocks;
import io.github.mortuusars.salt.event.ClientEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public class SaltClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        ClientEvents.registerTab();
        ClientEvents.registerModels();

        BlockRenderLayerMap.INSTANCE.putBlock(SaltBlocks.SMALL_SALT_BUD, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(SaltBlocks.MEDIUM_SALT_BUD, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(SaltBlocks.LARGE_SALT_BUD, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(SaltBlocks.SALT_CLUSTER, RenderType.cutout());
    }
}