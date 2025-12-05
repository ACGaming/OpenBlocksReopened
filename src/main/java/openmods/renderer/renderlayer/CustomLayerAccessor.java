package openmods.renderer.renderlayer;

public interface CustomLayerAccessor extends ICustomRenderLayer.OriginalLayer {
	default ICustomRenderLayer getCustomLayer() {
		return openmods$getCustomLayer();
	}

	ICustomRenderLayer openmods$getCustomLayer();

	void openmods$setCustomLayer(ICustomRenderLayer layer);
}
