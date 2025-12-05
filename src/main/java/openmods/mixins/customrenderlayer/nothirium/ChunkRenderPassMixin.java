package openmods.mixins.customrenderlayer.nothirium;

import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import openmods.mixins.EnumMixinUtil;
import openmods.renderer.renderlayer.CustomLayerAccessor;
import openmods.renderer.renderlayer.ICustomRenderLayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ChunkRenderPass.class, remap = false, priority = 100)
public class ChunkRenderPassMixin implements CustomLayerAccessor {
	@Final
	@Mutable
	@Shadow(aliases = { "$VALUES", "ENUM$VALUES" })
	private static ChunkRenderPass[] $VALUES;

	@Shadow
	@Final
	@Mutable
	public static ChunkRenderPass[] ALL;

	@Unique
	private ICustomRenderLayer openmods$customLayer;

	static {
		$VALUES = EnumMixinUtil.createNewVariantArray(
			$VALUES,
			ICustomRenderLayer.class,
			(layer, ordinal) -> {
				layer.setOrdinal(ChunkRenderPass.class, ordinal);
				ChunkRenderPass newLayer = openmods$invokeInit(layer.variantName(), ordinal);
				((CustomLayerAccessor) (Object) newLayer).openmods$setCustomLayer(layer);
				return newLayer;
			}
		);
		ALL = $VALUES;
	}

	@Invoker("<init>")
	private static ChunkRenderPass openmods$invokeInit(String internalName, int internalId) {
		throw new AssertionError();
	}

	@Override
	public ICustomRenderLayer openmods$getCustomLayer() {
		return openmods$customLayer;
	}

	@Override
	public void openmods$setCustomLayer(ICustomRenderLayer layer) {
		this.openmods$customLayer = layer;
	}
}

