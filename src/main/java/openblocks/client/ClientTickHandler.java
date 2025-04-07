package openblocks.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import openblocks.common.entity.EntityHangGlider;

public class ClientTickHandler {

	// TODO split
	private static int ticks = 0;

	@SubscribeEvent
	public void onRenderTickStart(TickEvent.RenderTickEvent evt) {
		if (evt.phase == Phase.START && Minecraft.getMinecraft().world != null) {
			EntityHangGlider.updateGliders();
		}
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent evt) {
		if (evt.phase == Phase.START && SoundEventsManager.isPlayerWearingGlasses()) {
			SoundEventsManager.instance.tickUpdate();
		}
		ticks++;
	}

	public static int getTicks() {
		return ticks;
	}
}
