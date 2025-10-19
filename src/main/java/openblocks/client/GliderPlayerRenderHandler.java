package openblocks.client;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import openblocks.common.entity.EntityHangGlider;

public class GliderPlayerRenderHandler {

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre evt) {
        final EntityPlayer player = evt.getEntityPlayer();
        if (EntityHangGlider.isGliderDeployed(player)) {
            player.limbSwing = 0;
            player.prevLimbSwingAmount = 0;
            player.limbSwingAmount = 0;
            applyRotation(player, evt.getPartialRenderTick(), -75);
        }
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post evt) {
        final EntityPlayer player = evt.getEntityPlayer();
        if (EntityHangGlider.isGliderDeployed(player)) {
            applyRotation(player, evt.getPartialRenderTick(), 75);
        }
    }

    private void applyRotation(EntityPlayer p, float partialTicks, float angle) {
        float yaw = interpolateRotation(p.prevRenderYawOffset, p.renderYawOffset, partialTicks);
        double rad = yaw * Math.PI / 180;
        GlStateManager.rotate(angle, (float) -Math.cos(rad), 0, (float) -Math.sin(rad));
    }

    private float interpolateRotation(float prev, float next, float partial) {
        float diff = next - prev;
        while (diff < -180) diff += 360;
        while (diff >= 180) diff -= 360;
        return prev + partial * diff;
    }
}
