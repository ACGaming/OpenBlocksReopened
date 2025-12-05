package openmods.mixins;

import zone.rong.mixinbooter.Context;

import java.util.List;

public enum Mixins {
	CUSTOM_RENDER_LAYERS(
		Static.MIXIN_HANDLER.create("custom_render_layers")
			.setDescription("Adds support for custom render layers")
			.early("mixins.openmods.customrenderlayer.minecraft.json")
			.lateMod(Mods.NOTHIRIUM, "mixins.openmods.customrenderlayer.nothirium.json")
	),
	SKY_BLOCK_RENDERING(
		Static.MIXIN_HANDLER.create("sky_block_rendering")
			.setDescription("Adds skybox rendering for sky blocks", "Requires \"custom_render_layers\"")
			.early("mixins.openblocks.skyblock.minecraft.json")
	),
	;

	private static class Static {
		public static final OpenModsMixinHandler MIXIN_HANDLER = new OpenModsMixinHandler("openblocks-reopened.mixin.properties");
	}

	Mixins(MixinBuilder mixin) {
		Static.MIXIN_HANDLER.registerMixin(mixin);
	}

	static {
		Static.MIXIN_HANDLER.generateDefaultConfigIfMissing();
	}

	public static List<String> getEarlyMixinConfigs() {
		return Static.MIXIN_HANDLER.getMixinConfigs(MixinBuilder.Phase.EARLY);
	}

	public static List<String> getLateMixinConfigs() {
		return Static.MIXIN_HANDLER.getMixinConfigs(MixinBuilder.Phase.LATE);
	}

	public static boolean shouldEarlyMixinConfigQueue(Context context) {
		return Static.MIXIN_HANDLER.shouldMixinConfigQueue(MixinBuilder.Phase.EARLY, context);
	}

	public static boolean shouldLateMixinConfigQueue(Context context) {
		return Static.MIXIN_HANDLER.shouldMixinConfigQueue(MixinBuilder.Phase.LATE, context);
	}
}
