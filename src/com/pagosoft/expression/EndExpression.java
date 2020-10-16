package com.pagosoft.expression;

class EndExpression extends Expression {
	public static final EndExpression INSTANCE = new EndExpression();

	public boolean accept(CharInput in) {
		return in.getPosition() == in.getEndPosition();
	}

	public String toString() { return "$"; }
}
