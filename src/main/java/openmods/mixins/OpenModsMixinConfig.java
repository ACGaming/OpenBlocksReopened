package openmods.mixins;

import info.openmods.openblocks.Tags;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class OpenModsMixinConfig {
	private static final Logger LOGGER = LogManager.getLogger(Tags.MOD_ID);

	private final String configFileName;
	private final Path propertiesFile;
	private final Properties properties;
	public final List<String> defaultConfigLines = new ArrayList<>();

	public OpenModsMixinConfig(String configFileName) {
		this.configFileName = configFileName;
		this.propertiesFile = Launch.minecraftHome.toPath()
			.resolve("config")
			.resolve(configFileName);
		this.properties = loadEarlyConfig();
		this.defaultConfigLines.add("# " + configFileName + " - Mixin configuration file");
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		String value = properties.getProperty(key);
		if (value == null) return defaultValue;
		return Boolean.parseBoolean(value);
	}

	public void registerConfig(String configName, boolean defaultEnabled, String... comments) {
		defaultConfigLines.add("#");
		if (comments != null) {
			for (String comment : comments) {
				defaultConfigLines.add("# " + comment);
			}
		}
		defaultConfigLines.add(configName + "=" + defaultEnabled);
	}

	public void generateDefaultConfigIfMissing() {
		if (Files.exists(propertiesFile)) return;

		try {
			Files.write(propertiesFile, defaultConfigLines);
		} catch (IOException e) {
			LOGGER.warn("Failed to create {}, using default config values", configFileName, e);
		}
	}

	private Properties loadEarlyConfig() {
		Properties properties = new Properties();
		if (!Files.exists(propertiesFile)) {
			LOGGER.warn("{} not found, a default config will be generated", configFileName);
			return properties;
		}

		try {
			properties.load(Files.newInputStream(propertiesFile));
		} catch (Exception e) {
			LOGGER.warn("Failed to load {}, using default config values", configFileName, e);
		}

		return properties;
	}
}

