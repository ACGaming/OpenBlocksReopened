package openmods.renderer.renderlayer;

import net.minecraft.util.BlockRenderLayer;
import openmods.renderer.shaders.ShaderProgram;

public interface ICustomRenderLayer {
	interface OriginalLayer {
		ICustomRenderLayer getCustomLayer();
	}

	/**
	 * Nothirium: Shader must have a <br>
	 * <code>uniform mat4 u_ModelViewProjectionMatrix</code>
	 */
	ShaderProgram shader();

	String variantName();

	<T extends Enum<T>> void setOrdinal(Class<T> enumClass, int ordinal);

	<T extends Enum<T>> int ordinal(Class<T> enumClass);

	BlockRenderLayer getBlockRenderLayer();

	default int initialBufferSize() {
		return 131072;
	}

	boolean preRenderLayer();

	void postRenderLayer();
}
