package com.pagosoft.expression;

class CharExpression extends Expression {
	private char ch;

	public CharExpression(char ch) {
		this.ch = ch;
	}

	public boolean accept(CharInput in) {
		return in.match(ch);
	}

	public String toString() { return String.valueOf(ch); }
}
