package com.pagosoft.expression;

class StartExpression extends Expression {
	public static final StartExpression INSTANCE = new StartExpression();

	public boolean accept(CharInput in) {
		return in.getPosition() == 0;
	}

	public String toString() { return "^"; }
}
