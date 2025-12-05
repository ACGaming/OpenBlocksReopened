package openmods.core;

import info.openmods.openblocks.Tags;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import openmods.Log;
import openmods.mixins.Mixins;
import zone.rong.mixinbooter.Context;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.List;
import java.util.Map;

//must be lower than all dependent ones
@SortingIndex(16)
@TransformerExclusions({ "openmods.asm.", "openmods.include.", "openmods.core.", "openmods.injector.", "openmods.Log" })
public class OpenModsCorePlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
	public static final String CORE_MARKER = "OpenModsCoreLoaded";

	public OpenModsCorePlugin() {
		Log.debug("<OpenModsLib %s>\\o", Tags.LIB_VERSION);
		Launch.blackboard.put(CORE_MARKER, Tags.LIB_VERSION);
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[] { "openmods.core.OpenModsClassTransformer" };
	}

	@Override
	public String getModContainerClass() {
		return "openmods.core.OpenModsCore";
	}

	@Override
	public String getSetupClass() {
		return "openmods.core.OpenModsHook";
	}

	@Override
	public void injectData(Map<String, Object> data) {
		Bootstrap.injectData(data);
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	@Override
	public List<String> getMixinConfigs() {
		return Mixins.getEarlyMixinConfigs();
	}

	@Override
	public boolean shouldMixinConfigQueue(Context context) {
		return Mixins.shouldEarlyMixinConfigQueue(context);
	}
}
