package openmods.renderer.renderlayer;

import net.minecraft.util.BlockRenderLayer;

import java.util.ArrayList;
import java.util.List;

public class CustomLayer {
	public static final ICustomRenderLayer[] LAYERS = findCustomBlockRenderLayers();

	private static ICustomRenderLayer[] findCustomBlockRenderLayers() {
		List<ICustomRenderLayer> customRenderLayers = new ArrayList<>();
		for (BlockRenderLayer layer : BlockRenderLayer.values()) {
			ICustomRenderLayer customLayer = ((ICustomRenderLayer.OriginalLayer) (Object) layer).getCustomLayer();
			if (customLayer != null) {
				customRenderLayers.add(customLayer);
			}
		}
		return customRenderLayers.toArray(new ICustomRenderLayer[0]);
	}
}
