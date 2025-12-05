package openmods.mixins;

import zone.rong.mixinbooter.Context;

import java.util.HashMap;
import java.util.Map;

public class MixinBuilder {
	final Map<String, ShouldMixinLoadCheck> earlyConfigs = new HashMap<>();
	final Map<String, ShouldMixinLoadCheck> lateConfigs = new HashMap<>();

	final String configName;

	boolean defaultEnabled = true;
	String[] description = null;

	@FunctionalInterface
	public interface ShouldMixinLoadCheck {
		boolean shouldLoad(Context context);
	}

	public enum Phase {
		EARLY,
		LATE,
	}

	private MixinBuilder(String configName) {
		this.configName = configName;
	}

	static MixinBuilder create(String configName) {
		return new MixinBuilder(configName);
	}

	public MixinBuilder disableByDefault() {
		this.defaultEnabled = false;
		return this;
	}

	public MixinBuilder setDescription(String... description) {
		this.description = description;
		return this;
	}

	private MixinBuilder put(Phase phase, ShouldMixinLoadCheck condition, String... configNames) {
		Map<String, ShouldMixinLoadCheck> targetMap = phase == Phase.EARLY ? earlyConfigs : lateConfigs;
		for (String configName : configNames) {
			targetMap.put(configName, condition);
		}
		return this;
	}

	public MixinBuilder early(ShouldMixinLoadCheck condition, String... configNames) {
		return put(Phase.EARLY, condition, configNames);
	}

	public MixinBuilder early(String... configNames) {
		return early(ctx -> true, configNames);
	}

	public MixinBuilder earlyMod(String modName, String... configNames) {
		return early(ctx -> ctx.isModPresent(modName), configNames);
	}

	public MixinBuilder late(ShouldMixinLoadCheck condition, String... configNames) {
		return put(Phase.LATE, condition, configNames);
	}

	public MixinBuilder late(String... configNames) {
		return late(ctx -> true, configNames);
	}

	public MixinBuilder lateMod(String modName, String... configNames) {
		return late(ctx -> ctx.isModPresent(modName), configNames);
	}
}
