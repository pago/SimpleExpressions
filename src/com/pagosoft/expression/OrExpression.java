package com.pagosoft.expression;

class OrExpression extends Expression {
	protected Expression[] exprs;

	public OrExpression(Expression[] exprs) {
		this.exprs = exprs;

		// the parser generates inefficient OrExpressions, we're going to fix that here
		if(exprs.length == 2 && exprs[1] instanceof OrExpression) {
			flatten(exprs[0], (OrExpression)exprs[1]);
		}
	}

	private void flatten(Expression head, OrExpression others) {
		exprs = new Expression[1 + others.exprs.length];
		exprs[0] = head;
		System.arraycopy(others.exprs, 0, exprs, 1, others.exprs.length);
	}

	/**
	 * matches if one of the expressions matches
	 * @param in
	 * @return
	 */
	public boolean accept(CharInput in) {
		for(Expression expr: exprs) {
			int m = in.mark();
			if(expr.accept(in)) {
				return true;
			} else {
				in.rewind(m);
			}
		}
		return false;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		boolean first = true;
		for(Expression expr: exprs) {
			if(!first) builder.append('|');
			builder.append(expr.toString());
			first = false;
		}
		return builder.append(')').toString();
	}
}
