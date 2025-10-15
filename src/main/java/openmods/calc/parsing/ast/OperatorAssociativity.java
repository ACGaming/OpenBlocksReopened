package openmods.calc.parsing.ast;

public enum OperatorAssociativity {
	LEFT {
		@Override
		public boolean isLessThan(int left, int right) {
			return left <= right;
		}
	},
	RIGHT {
		@Override
		public boolean isLessThan(int left, int right) {
			return left < right;
		}
	};

	public abstract boolean isLessThan(int left, int right);
}