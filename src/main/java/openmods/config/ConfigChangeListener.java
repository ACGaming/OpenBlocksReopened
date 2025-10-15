package openmods.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ConfigChangeListener {

	private final String modId;
	private final Configuration config;

	public ConfigChangeListener(String modId, Configuration config) {
		this.modId = modId;
		this.config = config;
	}

	@SubscribeEvent
	public void onConfigChange(OnConfigChangedEvent evt) {
		if (modId.equals(evt.getModID())) config.save();
	}

}
