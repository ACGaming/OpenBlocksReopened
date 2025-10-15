package openmods.calc.executable;

import openmods.calc.Frame;
import openmods.calc.FrameFactory;
import openmods.calc.parsing.ast.OperatorArity;
import openmods.calc.parsing.ast.OperatorAssociativity;
import openmods.calc.symbol.SymbolMap;
import openmods.calc.utils.Stack;

public abstract class BinaryOperator<E> extends Operator<E> {

	public static final OperatorAssociativity DEFAULT_ASSOCIATIVITY = OperatorAssociativity.LEFT;

	public final OperatorAssociativity associativity;

	@Override
	public OperatorArity arity() {
		return OperatorArity.BINARY;
	}

	private BinaryOperator(String id, int precedence, OperatorAssociativity associativity) {
		super(id, precedence);
		this.associativity = associativity;
	}

	private BinaryOperator(String id, int precendence) {
		this(id, precendence, DEFAULT_ASSOCIATIVITY);
	}

	public abstract static class Direct<E> extends BinaryOperator<E> {
		public Direct(String id, int precedence, OperatorAssociativity associativity) {
			super(id, precedence, associativity);
		}

		public Direct(String id, int precendence) {
			super(id, precendence);
		}

		public abstract E execute(E left, E right);

		@Override
		public final void execute(Frame<E> frame) {
			final Stack<E> stack = frame.stack();

			final E right = stack.pop();
			final E left = stack.pop();
			final E result = execute(left, right);
			stack.push(result);
		}
	}

	public abstract static class Scoped<E> extends BinaryOperator<E> {
		public Scoped(String id, int precedence, OperatorAssociativity associativity) {
			super(id, precedence, associativity);
		}

		public Scoped(String id, int precendence) {
			super(id, precendence);
		}

		public abstract E execute(SymbolMap<E> symbols, E left, E right);

		@Override
		public final void execute(Frame<E> frame) {
			final Stack<E> stack = frame.stack();

			final E right = stack.pop();
			final E left = stack.pop();
			final E result = execute(frame.symbols(), left, right);
			stack.push(result);
		}
	}

	public abstract static class StackBased<E> extends BinaryOperator<E> {
		public StackBased(String id, int precedence, OperatorAssociativity associativity) {
			super(id, precedence, associativity);
		}

		public StackBased(String id, int precendence) {
			super(id, precendence);
		}

		public abstract void executeOnStack(Frame<E> frame);

		@Override
		public final void execute(Frame<E> frame) {
			final Frame<E> executionFrame = FrameFactory.newLocalFrameWithSubstack(frame, 2);
			executeOnStack(executionFrame);
			executionFrame.stack().checkSizeIsExactly(1);
		}
	}

	@Override
	public boolean isLowerPriority(Operator<E> other) {
		return associativity.isLessThan(this.precedence, other.precedence);
	}

	@Override
	public String toString() {
		return "BinaryOperator [" + id + "]";
	}

}
