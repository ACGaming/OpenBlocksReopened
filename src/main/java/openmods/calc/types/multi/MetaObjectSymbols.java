package openmods.calc.types.multi;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import openmods.calc.Environment;
import openmods.calc.Frame;
import openmods.calc.symbol.BinaryFunction;
import openmods.calc.symbol.UnaryFunction;
import openmods.calc.types.multi.MetaObject.Slot;
import openmods.calc.types.multi.MetaObjectInfo.ISlotAccess;
import openmods.calc.types.multi.TypedFunction.RawReturn;
import openmods.calc.types.multi.TypedFunction.Variant;
import openmods.calc.utils.OptionalInt;
import openmods.calc.utils.Stack;
import java.util.List;
import java.util.Map;

public class MetaObjectSymbols {

	private static final String ATTR_NAME = "name";
	private static final String ATTR_INFO = "info";
	private static final String ATTR_CAPABILITIES = "slots";

	private static class SlotCheckSymbol extends BinaryFunction.Direct<TypedValue> {
		@Override
		protected TypedValue call(TypedValue left, TypedValue right) {
			final MetaObject mo = left.getMetaObject();
			final MetaObjectInfo.ISlotAccess info = right.as(MetaObjectInfo.ISlotAccess.class, "second 'can' argument");
			return left.domain.create(Boolean.class, info.checkIsPresent(mo));
		}
	}

	private static class MetaObjectSlot {
		private final MetaObject.Slot slot;

		private final MetaObjectInfo.ISlotAccess info;

		public MetaObjectSlot(MetaObject.Slot slot, MetaObjectInfo.ISlotAccess info) {
			this.slot = slot;
			this.info = info;
		}
	}

	private static MetaObject.Builder createBuilderFromArgs(Iterable<TypedValue> args) {
		final MetaObject.Builder result = MetaObject.builder();

		for (TypedValue arg : args) {
			if (arg.is(MetaObjectSlot.class)) {
				final MetaObjectSlot nativeSlot = arg.as(MetaObjectSlot.class);
				nativeSlot.info.set(result, nativeSlot.slot);
			} else if (arg.is(Cons.class)) {
				final Cons pair = arg.as(Cons.class);
				final MetaObjectInfo.ISlotAccess slotInfo = pair.car.as(MetaObjectInfo.ISlotAccess.class, "slot:value pair");
				final TypedValue slotValue = pair.cdr;

				if (slotValue.is(MetaObjectSlot.class)) {
					final MetaObjectSlot nativeSlot = slotValue.as(MetaObjectSlot.class);
					Preconditions.checkState(nativeSlot.info == slotInfo, "Invalid slot type for name %s, got %s", slotInfo.name(), nativeSlot.info.name());
					slotInfo.set(result, nativeSlot.slot);
				} else if (MetaObjectUtils.isCallable(slotValue)) {
					final Slot slot = slotInfo.wrap(slotValue);
					slotInfo.set(result, slot);
				} else {
					throw new IllegalArgumentException("Slot value must be native slot or callable");
				}
			} else {
				throw new IllegalArgumentException("Expected native slot or slot:value pair");
			}
		}

		return result;
	}

	public static void register(Environment<TypedValue> env) {
		final TypedValue nullValue = env.nullValue();
		final TypeDomain domain = nullValue.domain;
		domain.registerType(MetaObjectInfo.ISlotAccess.class, "metaobjectslot", createCapabilityMetaObject());
		domain.registerType(MetaObjectInfo.ISlotAccessProvider.class, "metasobjectslotprovider", createCapabilityProviderMetaObject());

		{
			final TypedValue metaObjectSlotType = domain.create(TypeUserdata.class, new TypeUserdata("metaobjectslotvalue", MetaObjectSlot.class));
			env.setGlobalSymbol("metaobjectslotvalue", metaObjectSlotType);
			domain.registerType(MetaObjectSlot.class, "metaobjectslotvalue", createMetaObjectSlotMetaObject(metaObjectSlotType));
		}

		{
			final TypedValue metaObjectType = domain.create(TypeUserdata.class, new TypeUserdata("metaobject", MetaObject.class),
					TypeUserdata.defaultMetaObject(domain)
							.set(new MetaObject.SlotCall() {
								@Override
								public void call(TypedValue self, OptionalInt argumentsCount, OptionalInt returnsCount, Frame<TypedValue> frame) {
									Preconditions.checkState(argumentsCount.isPresent(), "'metaobject' symbol requires arguments count");
									final Stack<TypedValue> stack = frame.stack().substack(argumentsCount.get());
									final MetaObject.Builder builder = createBuilderFromArgs(stack);
									stack.clear();
									stack.push(domain.create(MetaObject.class, builder.build()));

								}
							}).build());

			env.setGlobalSymbol("metaobject", metaObjectType);
			domain.registerType(MetaObject.class, "metaobject", createMetaObjectMetaObject(domain, metaObjectType, nullValue));
		}

		final Map<String, TypedValue> slots = Maps.newHashMap();

		for (Map.Entry<String, MetaObjectInfo.ISlotAccess> e : MetaObjectInfo.singleSlots.entrySet())
			slots.put(e.getKey(), domain.create(MetaObjectInfo.ISlotAccess.class, e.getValue()));

		for (Map.Entry<String, MetaObjectInfo.ISlotAccessProvider> e : MetaObjectInfo.mappedSlots.entrySet())
			slots.put(e.getKey(), domain.create(MetaObjectInfo.ISlotAccessProvider.class, e.getValue()));

		env.setGlobalSymbol(ATTR_CAPABILITIES, domain.create(SimpleNamespace.class, new SimpleNamespace(slots)));
		env.setGlobalSymbol("has", new SlotCheckSymbol());

		env.setGlobalSymbol("getmetaobject", new UnaryFunction.Direct<TypedValue>() {
			@Override
			protected TypedValue call(TypedValue value) {
				return value.domain.create(MetaObject.class, value.getMetaObject());
			}
		});

		env.setGlobalSymbol("setmetaobject", new BinaryFunction.Direct<TypedValue>() {
			@Override
			protected TypedValue call(TypedValue left, TypedValue right) {
				final MetaObject mo = right.as(MetaObject.class, "second 'setmetaobject' arg");
				return left.updateMetaObject(mo);
			}
		});

	}

	private static MetaObject createCapabilityMetaObject() {
		return MetaObject.builder()
				.set(new MetaObject.SlotDecompose() {
					@Override
					public Optional<List<TypedValue>> tryDecompose(TypedValue self, TypedValue input, int variableCount, Frame<TypedValue> frame) {
						final MetaObjectInfo.ISlotAccess info = self.as(MetaObjectInfo.ISlotAccess.class);
						final MetaObject mo = input.getMetaObject();

						if (info.checkIsPresent(mo)) {
							List<TypedValue> result = ImmutableList.of(input);
							return Optional.of(result);
						}

						return Optional.absent();
					}
				})
				.set(new MetaObject.SlotCall() {
					@Override
					public void call(TypedValue self, OptionalInt argumentsCount, OptionalInt returnsCount, Frame<TypedValue> frame) {
						final MetaObjectInfo.ISlotAccess info = self.as(MetaObjectInfo.ISlotAccess.class);

						final TypedValue target = frame.stack().peek(argumentsCount.get() - 1);

						final MetaObject mo = target.getMetaObject();
						final Slot slot = info.get(mo);
						Preconditions.checkState(slot != null, "Value %s has no slot %s", target);
						info.call(slot, frame, argumentsCount, returnsCount);
					}
				})
				.set(new MetaObject.SlotStr() {
					@Override
					public String str(TypedValue self, Frame<TypedValue> frame) {
						return ATTR_CAPABILITIES + "." + self.as(MetaObjectInfo.ISlotAccess.class).name();
					}
				})
				.set(new MetaObject.SlotRepr() {
					@Override
					public String repr(TypedValue self, Frame<TypedValue> frame) {
						return ATTR_CAPABILITIES + "." + self.as(MetaObjectInfo.ISlotAccess.class).name();
					}
				})
				.build();
	}

	private static MetaObject createCapabilityProviderMetaObject() {
		return MetaObject.builder()
				.set(new MetaObject.SlotCall() {
					@Override
					public void call(TypedValue self, OptionalInt argumentsCount, OptionalInt returnsCount, Frame<TypedValue> frame) {
						argumentsCount.compareIfPresent(1);
						returnsCount.compareIfPresent(1);

						final Stack<TypedValue> stack = frame.stack();
						final String key = stack.pop().as(String.class, "capability provider factory");
						final MetaObjectInfo.ISlotAccessProvider info = self.as(MetaObjectInfo.ISlotAccessProvider.class);
						final TypedValue capability = self.domain.create(MetaObjectInfo.ISlotAccess.class, info.create(key));
						stack.push(capability);
					}
				})
				.set(new MetaObject.SlotAttr() {
					@Override
					public Optional<TypedValue> attr(TypedValue self, String key, Frame<TypedValue> frame) {
						final MetaObjectInfo.ISlotAccessProvider info = self.as(MetaObjectInfo.ISlotAccessProvider.class);
						final TypedValue capability = self.domain.create(MetaObjectInfo.ISlotAccess.class, info.create(key));
						return Optional.of(capability);
					}
				})
				.set(new MetaObject.SlotStr() {
					@Override
					public String str(TypedValue self, Frame<TypedValue> frame) {
						return ATTR_CAPABILITIES + "." + self.as(MetaObjectInfo.ISlotAccess.class).name();
					}
				})
				.set(new MetaObject.SlotRepr() {
					@Override
					public String repr(TypedValue self, Frame<TypedValue> frame) {
						return ATTR_CAPABILITIES + "." + self.as(MetaObjectInfo.ISlotAccess.class).name();
					}
				})
				.build();
	}

	private static MetaObject createMetaObjectSlotMetaObject(TypedValue metaObjectSlotType) {
		return MetaObject.builder()
				.set(new MetaObject.SlotCall() {
					@Override
					public void call(TypedValue self, OptionalInt argumentsCount, OptionalInt returnsCount, Frame<TypedValue> frame) {
						final MetaObjectSlot slot = self.as(MetaObjectSlot.class);
						slot.info.call(slot.slot, frame, argumentsCount, returnsCount);
					}
				})
				.set(new MetaObject.SlotAttr() {
					@Override
					public Optional<TypedValue> attr(TypedValue self, String key, Frame<TypedValue> frame) {
						final MetaObjectSlot slot = self.as(MetaObjectSlot.class);
						final TypeDomain domain = self.domain;
						if (key.equals(ATTR_NAME)) return Optional.of(domain.create(String.class, slot.info.name()));
						if (key.equals(ATTR_INFO)) return Optional.of(domain.create(MetaObjectInfo.ISlotAccess.class, slot.info));
						return Optional.absent();
					}
				})
				.set(MetaObjectUtils.dirFromArray(ATTR_NAME, ATTR_INFO))
				.set(MetaObjectUtils.typeConst(metaObjectSlotType))
				.build();
	}

	private static TypedValue extractMetaSlotValue(MetaObject mo, MetaObjectInfo.ISlotAccess slotInfo, TypedValue nullValue) {
		final MetaObject.Slot slot = slotInfo.get(mo);
		if (slot == null) { return nullValue; }

		// optimization: if slot has original value (used to create it), return it
		if (slot instanceof MetaObject.SlotWithValue)
			return ((MetaObject.SlotWithValue)slot).getValue();

		// otherwise, just wrap existing slot (probably native)
		return nullValue.domain.create(MetaObjectSlot.class, new MetaObjectSlot(slot, slotInfo));
	}

	// much meta
	private static MetaObject createMetaObjectMetaObject(final TypeDomain domain, TypedValue metaObjectType, final TypedValue nullValue) {
		return MetaObject.builder()
				.set(new MetaObject.SlotAttr() {
					@Override
					public Optional<TypedValue> attr(TypedValue self, String key, Frame<TypedValue> frame) {
						final MetaObject mo = self.as(MetaObject.class);

						{
							final MetaObjectInfo.ISlotAccess slotInfo = MetaObjectInfo.singleSlots.get(key);
							if (slotInfo != null)
								return Optional.of(extractMetaSlotValue(mo, slotInfo, nullValue));
						}

						{
							final MetaObjectInfo.ISlotAccessProvider slotInfoProvider = MetaObjectInfo.mappedSlots.get(key);
							if (slotInfoProvider != null) {
								final TypedValue result = CallableValue.wrap(domain, new SimpleTypedFunction(domain) {
									@Variant
									@RawReturn
									public TypedValue call(String key) {
										final ISlotAccess slotInfo = slotInfoProvider.create(key);
										return extractMetaSlotValue(mo, slotInfo, nullValue);
									}
								});
								return Optional.of(result);
							}
						}

						return Optional.absent();
					}
				})
				.set(MetaObjectUtils.dirFromIterable(Iterables.concat(MetaObjectInfo.singleSlots.keySet(), MetaObjectInfo.mappedSlots.keySet())))
				.set(new MetaObject.SlotCall() {
					@Override
					public void call(TypedValue self, OptionalInt argumentsCount, OptionalInt returnsCount, Frame<TypedValue> frame) {
						Preconditions.checkState(argumentsCount.isPresent(), "'metaobject' symbol requires arguments count");
						final MetaObject mo = self.as(MetaObject.class);
						final Stack<TypedValue> stack = frame.stack().substack(argumentsCount.get());
						final MetaObject.Builder builder = createBuilderFromArgs(stack);
						stack.clear();
						stack.push(domain.create(MetaObject.class, builder.update(mo)));
					}
				})
				.set(MetaObjectUtils.typeConst(metaObjectType))
				.build();
	}

}
