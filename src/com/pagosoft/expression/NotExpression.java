package com.pagosoft.expression;

class NotExpression extends OrExpression {
	public NotExpression(Expression[] exprs) {
		super(exprs);
	}

	public boolean accept(CharInput in) {
		if(in.LA(1) == CharInput.EOF) return false;
		int m = in.mark();
		for(Expression expr: exprs) {
			if(expr.accept(in)) {
				in.rewind(m);
				return false;
			}
		}
		// no matter what, any of our child-expression can only consume one character at a time
		in.consume();
		return true;
	}

	public String toString() {
		return "~" + super.toString();
	}
}
