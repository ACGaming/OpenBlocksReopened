package openmods.mixins.customrenderlayer.minecraft;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.util.BlockRenderLayer;
import openmods.renderer.renderlayer.CustomLayer;
import openmods.renderer.renderlayer.ICustomRenderLayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RegionRenderCacheBuilder.class)
public class RegionRenderCacheBuilderMixin {
	@Shadow
	@Final
	private BufferBuilder[] worldRenderers;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void openmods$injectCustomLayerBuffers(CallbackInfo ci) {
		for (ICustomRenderLayer layer : CustomLayer.LAYERS) {
			int ordinal = layer.ordinal(BlockRenderLayer.class);
			this.worldRenderers[ordinal] = new BufferBuilder(layer.initialBufferSize());
		}
	}
}
