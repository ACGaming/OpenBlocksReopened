package openmods.calc.utils.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import openmods.calc.utils.DefaultMap;
import openmods.calc.utils.reflection.FieldWrapper;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class ConfigPropertyManager<T> {

	public static class NoSuchPropertyException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public NoSuchPropertyException(String message) {
			super(message);
		}
	}

	private static class FieldAdapter<T> {

		private final IStringReader<T> serializer;

		private final FieldWrapper<T> access;

		public FieldAdapter(IStringReader<T> serializer, FieldWrapper<T> access) {
			this.serializer = serializer;
			this.access = access;
		}

		public void set(Object instance, String value) {
			T converted = serializer.readFromString(value);
			access.set(instance, converted);
		}

		public String get(Object instance) {
			T value = access.get(instance);
			return String.valueOf(value);
		}

		public T getRaw(Object instance) {
			return access.get(instance);
		}

		public void setRaw(Object instance, Object value) {
			access.set(instance, access.getType().cast(value));
		}
	}

	private final Class<? extends T> cls;

	private final Map<String, FieldAdapter<?>> fields;

	public ConfigPropertyManager(Class<? extends T> cls) {
		this.cls = cls;

		ImmutableMap.Builder<String, FieldAdapter<?>> fields = ImmutableMap.builder();
		for (Field f : cls.getFields()) {
			ConfigProperty ann = f.getAnnotation(ConfigProperty.class);
			if (ann != null) {
				String name = ann.name();
				if (name.isEmpty()) name = f.getName();

				final FieldWrapper<?> access = FieldWrapper.create(f);
				final IStringReader<?> serializer = StringReader.get(f.getType());
				Preconditions.checkState(serializer != null, "Can't find serializer for field %s", f);

				@SuppressWarnings({ "rawtypes", "unchecked" })
				final FieldAdapter<?> adapter = new FieldAdapter(serializer, access);

				fields.put(name, adapter);
			}
		}

		this.fields = fields.build();
	}

	public Set<String> keys() {
		return fields.keySet();
	}

	private FieldAdapter<?> findField(String key) {
		final FieldAdapter<?> fieldAdapter = fields.get(key);
		if (fieldAdapter == null) throw new NoSuchPropertyException(String.format("Can't find key %s in class %s", key, cls));
		return fieldAdapter;
	}

	public String get(T instance, String key) {
		return findField(key).get(instance);
	}

	public void set(T instance, String key, String value) {
		findField(key).set(instance, value);
	}

	public Object getRaw(T instance, String key) {
		return findField(key).getRaw(instance);
	}

	public void setRaw(T instance, String key, Object value) {
		findField(key).setRaw(instance, value);
	}

	private static final DefaultMap<Class<?>, ConfigPropertyManager<?>> CACHE = new DefaultMap<Class<?>, ConfigPropertyManager<?>>() {
		@Override
		protected ConfigPropertyManager<?> create(Class<?> key) {
			return new ConfigPropertyManager<Object>(key);
		}
	};

	@SuppressWarnings("unchecked")
	public static <T> ConfigPropertyManager<T> getFor(Class<? extends T> cls) {
		return (ConfigPropertyManager<T>)CACHE.getOrCreate(cls);
	}
}
