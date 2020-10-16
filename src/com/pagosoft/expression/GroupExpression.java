package com.pagosoft.expression;

class GroupExpression extends Expression {
	private Expression[] exprs;

	public GroupExpression(Expression[] exprs) {
		this.exprs = exprs;
	}

	/**
	 * Accepts if all of its children accept.
	 * @param in
	 * @return
	 */
	public boolean accept(CharInput in) {
		int m = in.mark();
		for(Expression expr: exprs) {
			if(!expr.accept(in)) {
				in.rewind(m);
				return false;
			}
		}
		/*for(int i = 0; i < exprs.length; i++) {
			Expression expr = exprs[i];
			// special handling for RepeatedExpression as they need to know about the expression following them
			if(expr instanceof RepeatedExpression && i+1 < exprs.length) {
				if(!((RepeatedExpression)expr).accept(in, exprs[i+1])) {
					in.rewind(m);
					return false;
				} else {
					// the expression will only match if the next expression matches, too, thus we can just go on
					i++;
				}
			} else if(!expr.accept(in)) {
				in.rewind(m);
				return false;
			}
		}*/
		return true;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		for(Expression expr: exprs) {
			builder.append(expr.toString());
		}
		return builder.append(')').toString();
	}
}
