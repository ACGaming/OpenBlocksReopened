package openmods.calc.parsing.ast;

import com.google.common.collect.PeekingIterator;
import openmods.calc.parsing.token.Token;

public interface IAstParser<N> {
	public N parse(IParserState<N> state, PeekingIterator<Token> input);
}
