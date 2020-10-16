package com.pagosoft.expression;

class LimitedRepeatExpression extends Expression {
	private Expression expr;
	private int min, max;

	public LimitedRepeatExpression(Expression expr, int min, int max) {
		this.max = max;
		this.min = min;
		this.expr = expr;
	}

	/**
	 * Accepts if the input is matched at least once but will match as much of its expression
	 * as it finds
	 * @param in
	 * @return
	 */
	public boolean accept(CharInput in) {
		// eat minimum repetition
		int m = in.mark();
		for(int i = 0; i < min; i++) {
			if(!expr.accept(in)) {
				in.rewind(m);
				return false;
			}
		}
		if(min == max) return true;

		// there is no upper limit
		if(max == -1) {
			while(true) {
				m = in.mark();
				if(!expr.accept(in)) {
					in.rewind(m);
					break;
				}
			}
			return true;
		}

		// eat at most max more
		for(int i = min; i <= max; i++) {
			m = in.mark();
			if(!expr.accept(in)) {
				in.rewind(m);
				break;
			}
		}
		return true;
	}

	public String toString() { return String.format("%s{%s,%s}", expr.toString(), min, max); }
}
