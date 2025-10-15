package openmods.calc.executable;

import openmods.calc.Frame;

@FunctionalInterface
public interface IExecutable<E> {
	public void execute(Frame<E> frame);
}
