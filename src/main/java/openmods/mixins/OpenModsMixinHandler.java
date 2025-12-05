package openmods.mixins;

import zone.rong.mixinbooter.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenModsMixinHandler {
	private final Map<String, MixinBuilder.ShouldMixinLoadCheck> earlyConfigs = new HashMap<>();
	private final Map<String, MixinBuilder.ShouldMixinLoadCheck> lateConfigs = new HashMap<>();

	private final OpenModsMixinConfig mixinConfig;

	public OpenModsMixinHandler(String configFileName) {
		this.mixinConfig = new OpenModsMixinConfig(configFileName);
	}

	public MixinBuilder create(String configName) {
		return MixinBuilder.create(configName);
	}

	public void generateDefaultConfigIfMissing() {
		mixinConfig.generateDefaultConfigIfMissing();
	}

	private Map<String, MixinBuilder.ShouldMixinLoadCheck> getTargetMap(MixinBuilder.Phase phase) {
		return phase == MixinBuilder.Phase.EARLY ? earlyConfigs : lateConfigs;
	}

	public void registerMixin(MixinBuilder mixin) {
		mixinConfig.registerConfig(mixin.configName, mixin.defaultEnabled, mixin.description);

		boolean configEnabled = mixinConfig.getBoolean(mixin.configName, mixin.defaultEnabled);
		for (Map.Entry<String, MixinBuilder.ShouldMixinLoadCheck> entry : mixin.earlyConfigs.entrySet()) {
			earlyConfigs.put(entry.getKey(), context -> entry.getValue().shouldLoad(context) && configEnabled);
		}
		for (Map.Entry<String, MixinBuilder.ShouldMixinLoadCheck> entry : mixin.lateConfigs.entrySet()) {
			lateConfigs.put(entry.getKey(), context -> entry.getValue().shouldLoad(context) && configEnabled);
		}
	}

	public List<String> getMixinConfigs(MixinBuilder.Phase phase) {
		return new ArrayList<>(getTargetMap(phase).keySet());
	}

	public boolean shouldMixinConfigQueue(MixinBuilder.Phase phase, Context context) {
		return getTargetMap(phase)
			.getOrDefault(context.mixinConfig(), ctx -> false)
			.shouldLoad(context);
	}
}
