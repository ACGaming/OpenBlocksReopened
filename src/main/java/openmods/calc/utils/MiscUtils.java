package openmods.calc.utils;

import com.google.common.base.Preconditions;
import java.util.Map;

public class MiscUtils {

	public static <K, V> void putOnce(Map<K, V> map, K key, V value) {
		final V prev = map.put(key, value);
		Preconditions.checkState(prev == null, "Duplicate value on key %s: %s -> %s", key, prev, value);
	}
}
