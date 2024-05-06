package io.github.mortuusars.salt.mixin.client;

import io.github.mortuusars.salt.client.rendering.LayeredBakedModel;
import net.minecraft.client.renderer.ItemModelShaper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelShaper.class)
public class ItemModelMesherForgeMixin {
    @Inject(method = "rebuildCache", at = @At("HEAD"))
    private void onRebuildCache(CallbackInfo ci) {
        LayeredBakedModel.Cache.clear();
    }
}
