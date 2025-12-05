package openmods.mixins.customrenderlayer.nothirium;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL20;
import meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL42;
import meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL43;
import meldexun.nothirium.mc.renderer.chunk.RenderChunk;
import meldexun.nothirium.mc.util.BlockRenderLayerUtil;
import meldexun.nothirium.renderer.chunk.AbstractChunkRenderer;
import meldexun.renderlib.util.GLShader;
import net.minecraft.util.BlockRenderLayer;
import openmods.renderer.renderlayer.CustomLayerAccessor;
import openmods.renderer.renderlayer.ICustomRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {
	ChunkRendererGL20.class,
	ChunkRendererGL42.class,
	ChunkRendererGL43.class,
}, remap = false)
public abstract class ChunkRendererGLMixin extends AbstractChunkRenderer<RenderChunk> {
	@WrapOperation(method = "renderChunks", at = @At(value = "INVOKE", target = "Lmeldexun/renderlib/util/GLShader;use()V"))
	private void openmods$overwriteCustomLayerShader(
		GLShader instance,
		Operation<Void> original,
		ChunkRenderPass pass,
		@Share("customRenderLayer") LocalRef<ICustomRenderLayer> customRenderLayer
	) {
		BlockRenderLayer blockRenderLayer = BlockRenderLayerUtil.getBlockRenderLayer(pass);
		ICustomRenderLayer layer = ((CustomLayerAccessor) (Object) blockRenderLayer).getCustomLayer();

		if (layer != null && layer.preRenderLayer()) {
			customRenderLayer.set(layer);
		} else {
			original.call(instance);
		}
	}

	@WrapOperation(method = "renderChunks", at = @At(value = "INVOKE", target = "Lmeldexun/renderlib/util/GLShader;getUniform(Ljava/lang/String;)I"))
	private int openmods$redirectUniform(
		GLShader instance,
		String uniform,
		Operation<Integer> original,
		ChunkRenderPass pass,
		@Share("customRenderLayer") LocalRef<ICustomRenderLayer> customRenderLayer
	) {
		if (customRenderLayer.get() != null) {
			return customRenderLayer.get().shader().getUniformLocation(uniform);
		}
		return original.call(instance, uniform);
	}

	@WrapWithCondition(method = "renderChunks", at = @At(value = "INVOKE", target = "Lmeldexun/nothirium/mc/util/FogUtil;setupFogFromGL(Lmeldexun/renderlib/util/GLShader;)V"))
	private boolean openmods$customLayerUniforms(
		GLShader fogMode,
		ChunkRenderPass pass,
		@Share("customRenderLayer") LocalRef<ICustomRenderLayer> customRenderLayer
	) {
		return customRenderLayer.get() == null;
	}

	@Inject(method = "renderChunks", at = @At(value = "INVOKE", target = "Lmeldexun/renderlib/util/GLShader;pop()V"))
	private void openmods$postRenderCustomLayer(
		ChunkRenderPass pass,
		CallbackInfo ci,
		@Share("customRenderLayer") LocalRef<ICustomRenderLayer> customRenderLayer
	) {
		if (customRenderLayer.get() != null) {
			customRenderLayer.get().postRenderLayer();
		}
	}
}
