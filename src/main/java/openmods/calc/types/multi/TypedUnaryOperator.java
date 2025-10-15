package openmods.calc.types.multi;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import openmods.calc.Frame;
import openmods.calc.executable.UnaryOperator;
import openmods.calc.types.multi.MetaObject.SlotUnaryOp;
import openmods.calc.utils.Stack;
import openmods.calc.utils.reflection.TypeVariableHolder;
import openmods.calc.utils.reflection.TypeVariableHolderFiller;
import java.lang.reflect.TypeVariable;
import java.util.Map;

public class TypedUnaryOperator {

	public interface IOperation<A> {
		public TypedValue apply(TypeDomain domain, A value);
	}

	public interface ISimpleOperation<A, R> {
		public R apply(A value);
	}

	private interface IGenericOperation {
		public TypedValue apply(TypeDomain domain, TypedValue left);

		public void validate(TypeDomain domain);
	}

	public interface IDefaultOperation {
		public Optional<TypedValue> apply(TypeDomain domain, TypedValue value);
	}

	public static class Builder {
		private static class TypeVariableHolders {
			@TypeVariableHolder(IOperation.class)
			public static class Operation {
				public static TypeVariable<?> A;
			}

			@TypeVariableHolder(ISimpleOperation.class)
			public static class SimpleOperation {
				public static TypeVariable<?> A;
				public static TypeVariable<?> R;
			}
		}

		static {
			TypeVariableHolderFiller.instance.initialize(TypeVariableHolders.class);
		}

		private final String id;

		private final int precendence;

		private final Map<Class<?>, IGenericOperation> operations = Maps.newHashMap();

		private IDefaultOperation defaultOperation;

		private boolean addMetaObjectOverride = true;

		public Builder(String id, int precedence) {
			this.id = id;
			this.precendence = precedence;
		}

		@SuppressWarnings("unchecked")
		private static <T> Class<T> resolveVariable(TypeToken<?> token, TypeVariable<?> var) {
			return (Class<T>)token.resolveType(var).getRawType();
		}

		private Builder registerOperation(Class<?> argCls, final IGenericOperation op) {
			final IGenericOperation prev = operations.put(argCls, op);
			Preconditions.checkState(prev == null, "Duplicate operation registration on operator '%s' for type %s", id, argCls);
			return this;
		}

		public <A> Builder registerOperation(Class<? extends A> argCls, IOperation<? super A> op) {
			return registerOperation(argCls, createOperationWrapper(argCls, op));
		}

		private static <A> IGenericOperation createOperationWrapper(final Class<? extends A> argCls, final IOperation<? super A> op) {
			return new IGenericOperation() {
				@Override
				public TypedValue apply(TypeDomain domain, TypedValue argValue) {
					final A arg = argValue.unwrap(argCls);
					return op.apply(domain, arg);
				}

				@Override
				public void validate(TypeDomain domain) {
					Preconditions.checkState(domain.isKnownType(argCls), "Parameter type %s not in domain", argCls);
				}
			};
		}

		public <A, R> Builder registerOperation(Class<? extends A> argCls, Class<? super R> resultCls, ISimpleOperation<? super A, ? extends R> op) {
			return registerOperation(argCls, createOperationWrapper(argCls, resultCls, op));
		}

		private static <A, R> IGenericOperation createOperationWrapper(final Class<? extends A> argCls, final Class<? super R> resultCls, final ISimpleOperation<? super A, ? extends R> op) {
			return new IGenericOperation() {
				@Override
				public TypedValue apply(TypeDomain domain, TypedValue argValue) {
					final A value = argValue.unwrap(argCls);
					final R result = op.apply(value);
					return domain.create(resultCls, result);
				}

				@Override
				public void validate(TypeDomain domain) {
					Preconditions.checkState(domain.isKnownType(argCls), "Parameter type %s not in domain", argCls);
					Preconditions.checkState(domain.isKnownType(resultCls), "Return type %s not in domain", resultCls);
				}
			};
		}

		public <A> Builder registerOperation(IOperation<A> op) {
			final TypeToken<?> token = TypeToken.of(op.getClass());
			final Class<A> type = resolveVariable(token, TypeVariableHolders.Operation.A);
			return registerOperation(type, op);
		}

		public <A, R> Builder registerOperation(ISimpleOperation<A, R> op) {
			final TypeToken<?> token = TypeToken.of(op.getClass());
			final Class<A> argType = resolveVariable(token, TypeVariableHolders.SimpleOperation.A);
			final Class<R> resultType = resolveVariable(token, TypeVariableHolders.SimpleOperation.R);
			return registerOperation(argType, resultType, op);
		}

		public Builder setDefaultOperation(IDefaultOperation defaultOperation) {
			this.defaultOperation = defaultOperation;
			return this;
		}

		public Builder setNoMetaObjectOverride() {
			this.addMetaObjectOverride = false;
			return this;
		}

		public UnaryOperator<TypedValue> build(TypeDomain domain) {
			for (IGenericOperation op : operations.values())
				op.validate(domain);

			final Logic logic = new Logic(id, operations, defaultOperation, domain);

			return addMetaObjectOverride? new Meta(id, precendence, logic) : new NonMeta(id, precendence, logic);
		}
	}

	private static class Logic {

		private final String id;

		private final Map<Class<?>, IGenericOperation> operations;

		private final IDefaultOperation defaultOperation;

		private final TypeDomain domain;

		public Logic(String id, Map<Class<?>, IGenericOperation> operations, IDefaultOperation defaultOperation, TypeDomain domain) {
			this.id = id;
			this.operations = ImmutableMap.copyOf(operations);
			this.defaultOperation = defaultOperation;
			this.domain = domain;
		}

		public TypedValue execute(TypedValue value) {
			Preconditions.checkState(value.domain == this.domain, "Value belongs to different domain: %s", value);
			final IGenericOperation op = operations.get(value.type);
			if (op != null) return op.apply(value.domain, value);

			if (defaultOperation != null) {
				final Optional<TypedValue> result = defaultOperation.apply(value.domain, value);
				if (result.isPresent()) return result.get();
			}

			throw new IllegalArgumentException(String.format("Can't apply operation '%s' on value %s", id, value));
		}
	}

	private static class Meta extends UnaryOperator.StackBased<TypedValue> {
		private final Logic logic;

		public Meta(String id, int precedence, Logic logic) {
			super(id, precedence);
			this.logic = logic;
		}

		@Override
		public void executeOnStack(Frame<TypedValue> frame) {
			final Stack<TypedValue> stack = frame.stack();
			final TypedValue value = stack.pop();

			final SlotUnaryOp slotUnaryOp = value.getMetaObject().slotsUnaryOps.get(id);
			final TypedValue result;
			if (slotUnaryOp != null) {
				result = slotUnaryOp.op(value, frame);
			} else {
				result = logic.execute(value);
			}

			stack.push(result);
		}
	}

	private static class NonMeta extends UnaryOperator.Direct<TypedValue> {
		private final Logic logic;

		public NonMeta(String id, int precendence, Logic logic) {
			super(id, precendence);
			this.logic = logic;
		}

		@Override
		public TypedValue execute(TypedValue value) {
			return logic.execute(value);
		}
	}
}
