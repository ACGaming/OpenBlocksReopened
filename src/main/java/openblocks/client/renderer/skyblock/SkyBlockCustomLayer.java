package openblocks.client.renderer.skyblock;

import net.minecraft.util.BlockRenderLayer;
import openmods.renderer.renderlayer.ICustomRenderLayer;
import openmods.renderer.shaders.ShaderProgram;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class SkyBlockCustomLayer implements ICustomRenderLayer {
	private static SkyBlockCustomLayer INSTANCE;
	private final Map<Class<? extends Enum<?>>, Integer> ORDINALS = Collections.synchronizedMap(new WeakHashMap<>());

	private BlockRenderLayer layer;
	private boolean layerInitialized = false;

	public SkyBlockCustomLayer() {
		INSTANCE = this;
	}

	public static SkyBlockCustomLayer instance() {
		return INSTANCE;
	}

	@Override
	public ShaderProgram shader() {
		return SkyBlockRenderer.INSTANCE.shader();
	}

	@Override
	public String variantName() {
		return "SKY_BLOCK";
	}

	@Override
	public <T extends Enum<T>> void setOrdinal(Class<T> enumClass, int ordinal) {
		ORDINALS.put(enumClass, ordinal);
	}

	@Override
	public <T extends Enum<T>> int ordinal(Class<T> enumClass) {
		return ORDINALS.getOrDefault(enumClass, -1);
	}

	public BlockRenderLayer getBlockRenderLayer() {
		if (!layerInitialized) {
			layerInitialized = true;
			int ordinal = ordinal(BlockRenderLayer.class);
			layer = ordinal == -1 ? null : BlockRenderLayer.values()[ordinal];
		}
		return layer;
	}

	@Override
	public boolean preRenderLayer() {
		return SkyBlockRenderer.INSTANCE.preRenderLayer();
	}

	@Override
	public void postRenderLayer() {
		SkyBlockRenderer.INSTANCE.postRenderLayer();
	}
}
