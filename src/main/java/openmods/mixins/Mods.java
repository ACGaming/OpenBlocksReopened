package openmods.mixins;

import net.minecraftforge.fml.common.Loader;

public class Mods {
	public static final String MINECRAFT = "minecraft";
	public static final String NOTHIRIUM = "nothirium";

	public static class Loaded {
		private static final Loaded NOTHIRIUM = new Loaded(Mods.NOTHIRIUM);

		public static boolean nothirium() {return NOTHIRIUM.loaded();}

		private final String modId;
		private boolean loaded = false;
		private boolean initialized = false;

		Loaded(String modId) {
			this.modId = modId;
		}

		private boolean loaded() {
			if (!initialized) {
				initialized = true;
				loaded = Loader.isModLoaded(modId);
			}
			return loaded;
		}
	}
}
