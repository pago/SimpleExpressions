package com.pagosoft.expression;

class AnySymbolExpression extends Expression {
	public static final AnySymbolExpression INSTANCE = new AnySymbolExpression();

	public boolean accept(CharInput in) {
		in.consume();
		return true;
	}

	public String toString() { return "."; }
}
