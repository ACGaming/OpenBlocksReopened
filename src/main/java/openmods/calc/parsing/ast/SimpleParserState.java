package openmods.calc.parsing.ast;

import com.google.common.collect.PeekingIterator;
import openmods.calc.parsing.token.Token;
import java.util.List;

public abstract class SimpleParserState<N> implements IParserState<N> {

	private final IAstParser<N> parser;

	public SimpleParserState(IAstParser<N> parser) {
		this.parser = parser;
	}

	@Override
	public IAstParser<N> getParser() {
		return parser;
	}

	@Override
	public ISymbolCallStateTransition<N> getStateForSymbolCall(final String symbol) {
		return new ISymbolCallStateTransition<N>() {
			@Override
			public N createRootNode(List<N> children) {
				return createSymbolNode(symbol, children);
			}

			@Override
			public IParserState<N> getState() {
				return SimpleParserState.this;
			}
		};
	}

	@Override
	public IModifierStateTransition<N> getStateForModifier(final String modifier) {
		return new IModifierStateTransition<N>() {
			@Override
			public N createRootNode(N child) {
				return createModifierNode(modifier, child);
			}

			@Override
			public IParserState<N> getState() {
				return SimpleParserState.this;
			}
		};
	}

	public N parse(PeekingIterator<Token> tokens) {
		return parser.parse(this, tokens);
	}

	protected abstract N createModifierNode(String modifier, N child);

	protected abstract N createSymbolNode(String symbol, List<N> children);

}
