package com.pagosoft.expression;

class RangeExpression extends Expression {
	private char start, end;

	public RangeExpression(char start, char end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * Accepts if the character is within the given range
	 * @param in
	 * @return
	 */
	public boolean accept(CharInput in) {
		int ch = in.LA(1);
		if(start <= ch && ch <= end) {
			in.consume();
			return true;
		}
		return false;
	}

	public String toString() { return start + "-" + end; }
}
