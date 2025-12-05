package openmods.mixins;

import info.openmods.openblocks.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.WeakHashMap;

public final class EnumMixinUtil {
	private static final Logger LOGGER = LogManager.getLogger(Tags.MOD_ID);

	private static class ServiceLoaderCache {
		private final Map<Class<?>, ServiceLoader<?>> cache = new WeakHashMap<>();

		@SuppressWarnings("unchecked")
		public <T> ServiceLoader<T> get(Class<T> key) {
			return (ServiceLoader<T>) cache.computeIfAbsent(key, ServiceLoader::load);
		}
	}

	private static final ServiceLoaderCache SERVICE_LOADER_CACHE = new ServiceLoaderCache();

	private EnumMixinUtil() {}

	public interface VariantGenerator<ENUM extends Enum<ENUM>, INTERFACE> {
		ENUM createVariant(INTERFACE newVariant, int ordinal);
	}

	public static <ENUM extends Enum<ENUM>, SERVICE> ENUM[] createNewVariantArray(
		ENUM[] originalVariants,
		Class<SERVICE> serviceClass,
		VariantGenerator<ENUM, SERVICE> generator
	) {
		int ordinal = originalVariants[originalVariants.length - 1].ordinal() + 1;
		ArrayList<ENUM> variants = new ArrayList<>(Arrays.asList(originalVariants));

		ServiceLoader<SERVICE> customServices = SERVICE_LOADER_CACHE.get(serviceClass);
		Iterator<SERVICE> customServiceIter = customServices.iterator();
		while (customServiceIter.hasNext()) {
			SERVICE service;
			try {
				service = customServiceIter.next();
			} catch (Throwable throwable) {
				LOGGER.error("Failed to load an enum extension service implementation, skipping it", throwable);
				continue;
			}
			variants.add(generator.createVariant(service, ordinal));
			ordinal++;
		}

		return variants.toArray(originalVariants);
	}

	public static String upperSnakeCaseToTitleCase(String input) {
		String[] words = input.toLowerCase(Locale.ROOT).split("_");

		StringBuilder formattedName = new StringBuilder();
		for (String word : words) {
			formattedName.append(Character.toUpperCase(word.charAt(0)))
				.append(word.substring(1))
				.append(" ");
		}

		return formattedName.toString().trim();
	}
}
