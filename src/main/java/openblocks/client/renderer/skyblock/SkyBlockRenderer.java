package openblocks.client.renderer.skyblock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import openblocks.Config;
import openblocks.OpenBlocks;
import openmods.Log;
import openmods.mixins.Mods;
import openmods.reflection.MethodAccess;
import openmods.reflection.MethodAccess.Function0;
import openmods.renderer.shaders.ShaderProgram;
import openmods.renderer.shaders.ShaderProgramBuilder;
import openmods.utils.render.FramebufferBlitter;
import org.lwjgl.opengl.GL20;

public class SkyBlockRenderer implements IResourceManagerReloadListener {
	public static final SkyBlockRenderer INSTANCE = new SkyBlockRenderer();
	private static final ResourceLocation VERTEX_SOURCE_LOC = OpenBlocks.location("shaders/skyblock/" + renderBackend() + "/skyblock.vert");
	private static final ResourceLocation FRAGMENT_SOURCE_LOC = OpenBlocks.location("shaders/skyblock/" + renderBackend() + "/skyblock.frag");

	private Framebuffer skyFb;
	private boolean isActive;

	private ShaderProgram shader;

	public ShaderProgram shader() {
		return shader;
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		isActive = checkActivationConditions();
		if (isActive) {
			ShaderProgramBuilder builder = new ShaderProgramBuilder();
			builder.addShader(VERTEX_SOURCE_LOC, GL20.GL_VERTEX_SHADER);
			builder.addShader(FRAGMENT_SOURCE_LOC, GL20.GL_FRAGMENT_SHADER);
			if (Mods.Loaded.nothirium()) {
				builder.addAttribute("a_Pos", 0);
				builder.addAttribute("a_Offset", 4);
			}
			shader = builder.build();
			Log.info("Sky block renderer activated using %s backend", renderBackend());
		}
	}

	public boolean isActive() {
		return isActive;
	}

	private static String renderBackend() {
		return Mods.Loaded.nothirium() ? Mods.NOTHIRIUM : Mods.MINECRAFT;
	}

	private static boolean checkActivationConditions() {
		if (!Config.renderSkyBlocks) {
			Log.info("Disabled by config");
			return false;
		}

		// force class load
		BlockRenderLayer ignored = BlockRenderLayer.SOLID;

		if (SkyBlockCustomLayer.instance() == null) {
			Log.info("Custom sky block render layer was not initialized, did you enable custom_render_layers and sky_block_rendering?");
			return false;
		}

		if (SkyBlockCustomLayer.instance().getBlockRenderLayer() == null) {
			Log.severe("Custom sky block render layer is missing, this is not a good sign");
			return false;
		}

		if (FMLClientHandler.instance().hasOptifine() && optifineShadersEnabled()) {
			if (Config.skyBlocksOptifineOverride) {
				Log.warn("Optifine detected: skyblocks + shaders may hang your game");
			} else {
				Log.info("Disabled due to Optifine shaders (use `optifineOverride` config to override)");
				return false;
			}
		}

		if (!OpenGlHelper.isFramebufferEnabled()) {
			Log.info("Framebuffer not enabled");
			return false;
		}

		if (!FramebufferBlitter.INSTANCE.isValid()) {
			Log.info("Framebuffer blit not enabled");
			return false;
		}

		return true;
	}

	private static boolean optifineShadersEnabled() {
		try {
			final Class<?> config = Class.forName("Config");
			final Function0<Boolean> isShaders = MethodAccess.create(boolean.class, config, "isShaders");
			return isShaders.call(null);
		} catch (Exception e) {
			Log.info(e, "Failed to read Optifine config");
		}

		// can't tell, assume the worst
		return true;
	}

	public void preTerrainRenderHook() {
		final Framebuffer fb = Minecraft.getMinecraft().getFramebuffer();

		if (skyFb == null) {
			skyFb = new Framebuffer(fb.framebufferWidth, fb.framebufferHeight, false);
		} else if (skyFb.framebufferWidth != fb.framebufferWidth || skyFb.framebufferHeight != fb.framebufferHeight) {
			skyFb.createBindFramebuffer(fb.framebufferWidth, fb.framebufferHeight);
		}

		FramebufferBlitter.INSTANCE.blitFramebuffer(fb, skyFb);

		fb.bindFramebuffer(false);
	}

	public boolean preRenderLayer() {
		if (!isActive) return false;

		shader.bind();
		final Minecraft mc = Minecraft.getMinecraft();
		shader.uniform1i("skyboxTexture", 0);
		shader.uniform2f("screenSize", mc.displayWidth, mc.displayHeight);

		GlStateManager.disableAlpha();
		skyFb.bindFramebufferTexture();
		return true;
	}

	public void postRenderLayer() {
		shader.release();

		GlStateManager.enableAlpha();
	}
}
