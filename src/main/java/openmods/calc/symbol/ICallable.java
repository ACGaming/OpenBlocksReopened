package openmods.calc.symbol;

import openmods.calc.Frame;
import openmods.calc.utils.OptionalInt;

@FunctionalInterface
public interface ICallable<E> {
	public void call(Frame<E> frame, OptionalInt argumentsCount, OptionalInt returnsCount);
}