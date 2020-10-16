package com.pagosoft.expression;

class SymbolExpression extends Expression {
	private char[] symbol;
	public SymbolExpression(CharSequence symbol) {
		this.symbol = Expression.sequenceToArray(symbol);
	}

	public boolean accept(CharInput in) {
		return in.match(symbol);
	}

	public String toString() { return symbol.toString(); }
}
