package openblocks.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import openblocks.OpenBlocks;
import openblocks.common.entity.EntityHangGlider;
import openmods.renderer.DisplayListWrapper;

public class EntityHangGliderRenderer extends Render<EntityHangGlider> {

	private static final float QUAD_HALF_SIZE = 2.4f;
	private static final float ONGROUND_ROTATION = 90f;

	private final DisplayListWrapper gliderRender = new DisplayListWrapper() {
		@Override
		public void compile() {
			GlStateManager.disableCull();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.glBegin(0x7); // GL_QUADS

			GlStateManager.glTexCoord2f(1, 1);
			GlStateManager.glVertex3f(QUAD_HALF_SIZE, 0, QUAD_HALF_SIZE);

			GlStateManager.glTexCoord2f(0, 1);
			GlStateManager.glVertex3f(-QUAD_HALF_SIZE, 0, QUAD_HALF_SIZE);

			GlStateManager.glTexCoord2f(0, 0);
			GlStateManager.glVertex3f(-QUAD_HALF_SIZE, 0, -QUAD_HALF_SIZE);

			GlStateManager.glTexCoord2f(1, 0);
			GlStateManager.glVertex3f(QUAD_HALF_SIZE, 0, -QUAD_HALF_SIZE);

			GlStateManager.glEnd();
			GlStateManager.enableCull();
		}
	};

	private static final ResourceLocation texture = OpenBlocks.location("textures/models/hang_glider.png");

	public EntityHangGliderRenderer(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityHangGlider glider, double x, double y, double z, float f, float f1) {
		final EntityPlayer owner = glider.getPlayer();
		if (owner == null) return;

		final Minecraft minecraft = Minecraft.getMinecraft();
		final boolean isLocalPlayer = owner.isUser();
		final boolean isFpp = minecraft.gameSettings.thirdPersonView == 0;
		final boolean isDeployed = glider.isDeployed();

		if (isLocalPlayer && isFpp && !isDeployed) return;

		final float rotation = interpolateRotation(glider.prevRotationYaw, glider.rotationYaw, f1);

		GlStateManager.pushMatrix();

		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(180.0F - rotation, 0.0F, 1.0F, 0.0F);
		if (isLocalPlayer && isFpp) {
			// move over head when flying in FPP
			GlStateManager.translate(0, +0.7, 0);
		} else {
			if (!isDeployed) {
				// move up little bit
				GlStateManager.translate(0, +0.2, +0.3);
			} else {
				// move closer to back and forward when flying
				GlStateManager.translate(0, -0.5, -1.0);
			}
		}

		if (!isDeployed) {
			GlStateManager.rotate(ONGROUND_ROTATION, 1, 0, 0);
			GlStateManager.scale(0.4f, 1f, 0.4f);
		}

		bindTexture(texture);
		gliderRender.render();
		GlStateManager.popMatrix();
	}

	private static float interpolateRotation(float prevRotation, float nextRotation, float modifier) {
		float rotation = nextRotation - prevRotation;

		while (rotation < -180.0F)
			rotation += 360.0F;

		while (rotation >= 180.0F) {
			rotation -= 360.0F;
		}

		return prevRotation + modifier * rotation;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityHangGlider entity) {
		return texture;
	}
}
