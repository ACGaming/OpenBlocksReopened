package openmods.mixins.customrenderlayer.minecraft;

import net.minecraft.util.BlockRenderLayer;
import openmods.mixins.EnumMixinUtil;
import openmods.renderer.renderlayer.CustomLayerAccessor;
import openmods.renderer.renderlayer.ICustomRenderLayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = BlockRenderLayer.class, priority = 100)
public class BlockRenderLayerMixin implements CustomLayerAccessor {
	@Final
	@Mutable
	@Shadow(aliases = { "$VALUES", "ENUM$VALUES" })
	private static BlockRenderLayer[] $VALUES;

	@Unique
	private ICustomRenderLayer openmods$customLayer;

	static {
		$VALUES = EnumMixinUtil.createNewVariantArray(
			$VALUES,
			ICustomRenderLayer.class,
			(ICustomRenderLayer layer, int ordinal) -> {
				String variantName = layer.variantName();
				String layerName = EnumMixinUtil.upperSnakeCaseToTitleCase(variantName);
				layer.setOrdinal(BlockRenderLayer.class, ordinal);
				BlockRenderLayer newLayer = openmods$invokeInit(variantName, ordinal, layerName);
				((CustomLayerAccessor) (Object) newLayer).openmods$setCustomLayer(layer);
				return newLayer;
			}
		);
	}

	@Invoker("<init>")
	private static BlockRenderLayer openmods$invokeInit(String internalName, int internalId, String layerName) {
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
