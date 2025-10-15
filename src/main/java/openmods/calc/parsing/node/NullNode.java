package openmods.calc.parsing.node;

import com.google.common.collect.ImmutableList;
import openmods.calc.executable.IExecutable;
import java.util.List;

public class NullNode<E> implements IExprNode<E> {

	@Override
	public void flatten(List<IExecutable<E>> output) {}

	@Override
	public Iterable<IExprNode<E>> getChildren() {
		return ImmutableList.of();
	}

}
