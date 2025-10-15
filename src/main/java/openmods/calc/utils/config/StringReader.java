package openmods.calc.utils.config;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public abstract class StringReader {

	public static final IStringReader<Integer> INTEGER = new IStringReader<Integer>() {

		@Override
		public Integer readFromString(String s) {
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				throw new StringConversionException("integer", s, e);
			}
		}
	};

	public static final IStringReader<Float> FLOAT = new IStringReader<Float>() {

		@Override
		public Float readFromString(String s) {
			try {
				return Float.parseFloat(s);
			} catch (NumberFormatException e) {
				throw new StringConversionException("float", s, e);
			}
		}

	};

	public static final IStringReader<Double> DOUBLE = new IStringReader<Double>() {

		@Override
		public Double readFromString(String s) {
			try {
				return Double.parseDouble(s);
			} catch (NumberFormatException e) {
				throw new StringConversionException("double", s, e);
			}
		}
	};

	public static final IStringReader<String> STRING = new IStringReader<String>() {

		@Override
		public String readFromString(String s) {
			return s;
		}
	};

	public static final IStringReader<Short> SHORT = new IStringReader<Short>() {

		@Override
		public Short readFromString(String s) {
			try {
				return Short.parseShort(s);
			} catch (NumberFormatException e) {
				throw new StringConversionException("short", s, e);
			}
		}
	};

	public static final IStringReader<Byte> BYTE = new IStringReader<Byte>() {

		@Override
		public Byte readFromString(String s) {
			try {
				return Byte.parseByte(s);
			} catch (NumberFormatException e) {
				throw new StringConversionException("byte", s, e);
			}
		}
	};

	public static final IStringReader<Boolean> BOOL = new IStringReader<Boolean>() {

		@Override
		public Boolean readFromString(String s) {
			if (s.equalsIgnoreCase("true")) return Boolean.TRUE;
			else if (s.equalsIgnoreCase("false")) return Boolean.FALSE;

			throw new StringConversionException("bool", s, "true", "false");
		}
	};

	public static final IStringReader<Long> LONG = new IStringReader<Long>() {

		@Override
		public Long readFromString(String s) {
			try {
				return Long.parseLong(s);
			} catch (NumberFormatException e) {
				throw new StringConversionException("long", s, e);
			}
		}
	};

	public static final Map<Class<?>, IStringReader<?>> STRING_SERIALIZERS = ImmutableMap.<Class<?>, IStringReader<?>> builder()
			.put(Integer.class, INTEGER)
			.put(int.class, INTEGER)
			.put(Boolean.class, BOOL)
			.put(boolean.class, BOOL)
			.put(Byte.class, BYTE)
			.put(byte.class, BYTE)
			.put(Double.class, DOUBLE)
			.put(double.class, DOUBLE)
			.put(Float.class, FLOAT)
			.put(float.class, FLOAT)
			.put(Long.class, LONG)
			.put(long.class, LONG)
			.put(Short.class, SHORT)
			.put(short.class, SHORT)
			.put(String.class, STRING)
			.build();

	@SuppressWarnings("unchecked")
	public static <T> IStringReader<T> get(Class<? extends T> cls) {
		return (IStringReader<T>)STRING_SERIALIZERS.get(cls);
	}
}
