package openmods.calc.parsing.node;

import com.google.common.collect.ImmutableList;
import openmods.calc.executable.IExecutable;
import java.util.List;

public class SingleExecutableNode<E> implements IExprNode<E> {
	private final IExecutable<E> value;

	public SingleExecutableNode(IExecutable<E> value) {
		this.value = value;
	}

	@Override
	public void flatten(List<IExecutable<E>> output) {
		output.add(value);
	}

	@Override
	public Iterable<IExprNode<E>> getChildren() {
		return ImmutableList.of();
	}
}
