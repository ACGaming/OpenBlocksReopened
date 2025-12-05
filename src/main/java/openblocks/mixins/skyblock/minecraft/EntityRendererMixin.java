package openblocks.mixins.skyblock.minecraft;

import net.minecraft.client.renderer.EntityRenderer;
import openblocks.client.renderer.skyblock.SkyBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	@Inject(method = "renderWorldPass", at = @At(
		value = "INVOKE_STRING",
		target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
		args = "ldc=prepareterrain",
		shift = At.Shift.AFTER
	))
	private void onRenderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
		SkyBlockRenderer.INSTANCE.preTerrainRenderHook();
	}
}
