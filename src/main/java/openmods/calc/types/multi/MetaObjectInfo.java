package openmods.calc.types.multi;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.reflect.TypeToken;
import openmods.calc.Frame;
import openmods.calc.types.multi.MetaObject.Builder;
import openmods.calc.types.multi.MetaObject.Slot;
import openmods.calc.types.multi.MetaObject.SlotAdapter;
import openmods.calc.types.multi.MetaObject.SlotField;
import openmods.calc.utils.OptionalInt;
import openmods.calc.utils.reflection.TypeVariableHolder;
import openmods.calc.utils.reflection.TypeVariableHolderFiller;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Locale;
import java.util.Map;

public class MetaObjectInfo {

	private static class TypeVariableHolders {
		@TypeVariableHolder(SlotAdapter.class)
		public static class SlotAdapterVars {
			public static TypeVariable<?> T;
		}

		@TypeVariableHolder(Map.class)
		public static class MapVars {
			public static TypeVariable<?> K;
			public static TypeVariable<?> V;
		}
	}

	static {
		TypeVariableHolderFiller.instance.initialize(TypeVariableHolders.class);
	}

	public interface ISlotAccess {
		public String name();

		public boolean checkIsPresent(MetaObject mo);

		public void call(Slot slot, Frame<TypedValue> frame, OptionalInt argumentsCount, OptionalInt returnsCount);

		public Slot get(MetaObject mo);

		public void set(MetaObject.Builder builder, Slot slot);

		public Slot wrap(TypedValue slotValue);
	}

	public interface ISlotAccessProvider {
		public ISlotAccess create(String key);
	}

	private static class SingleSlotAccess implements ISlotAccess {
		private final String name;
		private final SlotAdapter<Slot> adapter;

		private final Field field;
		private final Method builderMethod;

		public SingleSlotAccess(final Field field, Method builderMethod, String name, SlotAdapter<Slot> adapter) {
			this.field = field;
			this.name = name;
			this.adapter = adapter;
			this.builderMethod = builderMethod;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public boolean checkIsPresent(MetaObject mo) {
			try {
				return field.get(mo) != null;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void call(Slot slot, Frame<TypedValue> frame, OptionalInt argumentsCount, OptionalInt returnsCount) {
			adapter.call(slot, frame, argumentsCount, returnsCount);
		}

		@Override
		public Slot wrap(TypedValue slotValue) {
			return adapter.wrap(slotValue);
		}

		@Override
		public Slot get(MetaObject mo) {
			try {
				return (Slot)field.get(mo);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void set(MetaObject.Builder builder, Slot slot) {
			try {
				builderMethod.invoke(builder, slot);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static final Map<String, ISlotAccess> singleSlots;

	private static class MappedSlotAccessFactory implements ISlotAccessProvider {
		public final String name;
		public final SlotAdapter<Slot> adapter;

		private final Field mapField;
		private final Method builderMethod;

		public MappedSlotAccessFactory(Field mapField, Method builderMethod, String name, SlotAdapter<Slot> adapter) {
			this.name = name;
			this.adapter = adapter;
			this.mapField = mapField;
			this.builderMethod = builderMethod;
		}

		@Override
		public ISlotAccess create(String key) {
			return new Access(key);
		}

		public class Access implements ISlotAccess {
			private final String name;
			private final String key;

			@SuppressWarnings("unchecked")
			private Map<String, ? extends Slot> getMap(MetaObject mo) throws IllegalAccessException {
				return (Map<String, ? extends Slot>)mapField.get(mo);
			}

			public Access(final String key) {
				this.key = key;
				this.name = MappedSlotAccessFactory.this.name + ":" + key;
			}

			@Override
			public String name() {
				return name;
			}

			@Override
			public boolean checkIsPresent(MetaObject mo) {
				try {
					return getMap(mo).containsKey(key);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void call(Slot slot, Frame<TypedValue> frame, OptionalInt argumentsCount, OptionalInt returnsCount) {
				adapter.call(slot, frame, argumentsCount, returnsCount);
			}

			@Override
			public Slot wrap(TypedValue slotValue) {
				return adapter.wrap(slotValue);
			}

			@Override
			public Slot get(MetaObject mo) {
				try {
					return getMap(mo).get(key);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void set(MetaObject.Builder builder, Slot slot) {
				try {
					builderMethod.invoke(builder, key, slot);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		}

	}

	public static final Map<String, ISlotAccessProvider> mappedSlots;

	static {
		final ImmutableMap.Builder<String, ISlotAccess> singleSlotsBuilder = ImmutableMap.builder();
		final ImmutableMap.Builder<String, ISlotAccessProvider> mappedSlotsBuilder = ImmutableMap.builder();

		final Map<Class<?>, Method> singleSlotBuilderMethods = Maps.newHashMap();
		final Map<Class<?>, Method> mappedSlotBuilderMethods = Maps.newHashMap();
		for (Method m : Builder.class.getDeclaredMethods()) {
			if (m.getName().equals("set")) {
				final Class<?>[] parameterTypes = m.getParameterTypes();
				if (parameterTypes.length == 1) {
					final Class<?> slotCls = parameterTypes[0];
					Preconditions.checkState(Slot.class.isAssignableFrom(slotCls), "Invalid builder method: %s", m);
					singleSlotBuilderMethods.put(slotCls, m);
				} else if (parameterTypes.length == 2) {
					Preconditions.checkState(parameterTypes[0] == String.class, "Invalid builder method: %s", m);
					final Class<?> slotCls = parameterTypes[1];
					Preconditions.checkState(Slot.class.isAssignableFrom(slotCls), "Invalid builder method: %s", m);
					mappedSlotBuilderMethods.put(slotCls, m);
				} else {
					throw new IllegalArgumentException("Invalid builder method: " + m);
				}
			}
		}

		for (Field f : MetaObject.class.getDeclaredFields()) {
			final SlotField annotation = f.getAnnotation(SlotField.class);
			if (annotation != null) {
				final String fieldName = f.getName();
				if (fieldName.startsWith("slots")) {
					final String slotName = fieldName.substring("slots".length());
					final String lcSlotName = slotName.toLowerCase(Locale.ROOT);
					final TypeToken<?> mapType = TypeToken.of(f.getGenericType());
					final Class<?> keyCls = mapType.resolveType(TypeVariableHolders.MapVars.K).getRawType();
					Preconditions.checkState(keyCls == String.class, "Invalid slot field: %s", f);
					final Class<?> slotCls = mapType.resolveType(TypeVariableHolders.MapVars.V).getRawType();
					final SlotAdapter<Slot> adapter = createAdapterInstance(slotCls, annotation);
					final Method builderMethod = mappedSlotBuilderMethods.get(slotCls);
					Preconditions.checkState(builderMethod != null, "Missing builder method for %s", lcSlotName);
					mappedSlotsBuilder.put(lcSlotName, new MappedSlotAccessFactory(f, builderMethod, lcSlotName, adapter));
				} else if (fieldName.startsWith("slot")) {
					final String slotName = fieldName.substring("slot".length());
					final String lcSlotName = slotName.toLowerCase(Locale.ROOT);
					final Class<?> slotCls = f.getType();
					final SlotAdapter<Slot> adapter = createAdapterInstance(slotCls, annotation);
					final Method builderMethod = singleSlotBuilderMethods.get(slotCls);
					Preconditions.checkState(builderMethod != null, "Missing builder method for %s", lcSlotName);
					singleSlotsBuilder.put(lcSlotName, new SingleSlotAccess(f, builderMethod, lcSlotName, adapter));
				} else {
					throw new AssertionError("Invalid slot name: " + fieldName);
				}
			}
		}

		singleSlots = singleSlotsBuilder.build();
		mappedSlots = mappedSlotsBuilder.build();

		final SetView<String> commonSlotNames = Sets.intersection(singleSlots.keySet(), mappedSlots.keySet());
		Preconditions.checkState(commonSlotNames.isEmpty(), "Duplicate slots: %s", commonSlotNames);
	}

	@SuppressWarnings("unchecked")
	private static SlotAdapter<Slot> createAdapterInstance(Class<?> slotCls, SlotField annotation) {
		final Class<? extends SlotAdapter<? extends Slot>> adapterCls = annotation.adapter();
		final Class<?> slotAdapterTarget = TypeToken.of(adapterCls).resolveType(TypeVariableHolders.SlotAdapterVars.T).getRawType();
		Preconditions.checkState(slotAdapterTarget == slotCls, "Invalid slot adapter type: expected %s, got %s", slotCls, slotAdapterTarget);
		try {
			return (SlotAdapter<Slot>)adapterCls.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

}
