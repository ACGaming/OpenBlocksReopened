package openmods.core;

import openmods.mixins.Mixins;
import zone.rong.mixinbooter.Context;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;

public class OpenModsLateMixinLoader implements ILateMixinLoader {
	@Override
	public List<String> getMixinConfigs() {
		return Mixins.getLateMixinConfigs();
	}

	@Override
	public boolean shouldMixinConfigQueue(Context context) {
		return Mixins.shouldLateMixinConfigQueue(context);
	}
}
