package com.pagosoft.expression;

class OptionalExpression extends Expression {
	private Expression expr;

	public OptionalExpression(Expression expr) {
		this.expr = expr;
	}

	public boolean accept(CharInput in) {
		int m = in.mark();
		if(!expr.accept(in)) {
			in.rewind(m);
		}
		return true;
	}

	public String toString() {
		if(expr instanceof RepeatedExpression) {
			String s = expr.toString();
			return s.substring(0, s.length()-1) + "*";
		}
		return expr.toString() + "?";
	}
}
