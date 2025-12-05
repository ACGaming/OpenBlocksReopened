package openmods.mixins.customrenderlayer.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import openmods.renderer.renderlayer.CustomLayer;
import openmods.renderer.renderlayer.ICustomRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	@Inject(
		method = "renderWorldPass",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/texture/ITextureObject;restoreLastBlurMipmap()V",
			ordinal = 1,
			shift = At.Shift.AFTER
		)
	)
	private void openmods$renderCustomLayers(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
		final Minecraft mc = Minecraft.getMinecraft();
		final Entity rwEntity = mc.getRenderViewEntity();
		if (rwEntity == null) return;

		for (ICustomRenderLayer layer : CustomLayer.LAYERS) {
			if (layer.preRenderLayer()) {
				mc.renderGlobal.renderBlockLayer(layer.getBlockRenderLayer(), partialTicks, pass, rwEntity);
				mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				layer.postRenderLayer();
			}
		}
	}
}
