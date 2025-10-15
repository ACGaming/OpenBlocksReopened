package openmods.calc.executable;

import openmods.calc.parsing.ast.IOperator;

public abstract class Operator<E> implements IExecutable<E>, IOperator<Operator<E>> {

	public final String id;

	public final int precedence;

	public Operator(String id, int precedence) {
		this.id = id;
		this.precedence = precedence;
	}

	@Override
	public String id() {
		return id;
	}

}
