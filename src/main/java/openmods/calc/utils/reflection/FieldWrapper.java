package openmods.calc.utils.reflection;

import java.lang.reflect.Field;

public class FieldWrapper<T> {

	public static class FieldAccessException extends RuntimeException {
		private static final long serialVersionUID = 3261757597754500600L;

		private static String createMessage(Field f, String action) {
			return "Failed to " + action + " field " + f;
		}

		public FieldAccessException(Field f, String action, Throwable cause) {
			super(createMessage(f, action), cause);
		}

		public FieldAccessException(Field f, String action) {
			super(createMessage(f, action));
		}

	}

	public final Field field;

	private FieldWrapper(Field field) {
		this.field = field;
		field.setAccessible(true);
	}

	@SuppressWarnings("unchecked")
	public T get(Object target) {
		try {
			return (T)field.get(target);
		} catch (Throwable t) {
			throw new FieldAccessException(field, "read", t);
		}
	}

	public void set(Object target, T value) {
		try {
			field.set(target, value);
		} catch (Throwable t) {
			throw new FieldAccessException(field, "set", t);
		}
	}

	@SuppressWarnings("unchecked")
	public Class<T> getType() {
		return (Class<T>)field.getType();
	}

	public static <T> FieldWrapper<T> create(Field f) {
		return new FieldWrapper<T>(f);
	}
}
