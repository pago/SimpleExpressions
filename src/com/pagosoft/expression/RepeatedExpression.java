package com.pagosoft.expression;

/**
 * A -> a | aA
 */
class RepeatedExpression extends Expression {
	private Expression expr;

	public RepeatedExpression(Expression expr) {
		this.expr = expr;
	}

	/**
	 * Accepts if the input is matched at least once but will match as much of its expression
	 * as it finds
	 * @param in
	 * @return
	 */
	public boolean accept(CharInput in) {
		int m = in.mark();
		if(!expr.accept(in)) {
			in.rewind(m);
			return false;
		}

		while(in.LA(1) != CharInput.EOF) {
			m = in.mark();
			if(!expr.accept(in)) {
				in.rewind(m);
				break;
			}
		}
		return true;
	}

	/*
	public boolean accept(CharInput in, Expression[] next, int thisOne) {
		int m = in.mark();
		if(!expr.accept(in)) {
			in.rewind(m);
			return false;
		}

		Node head = null;
		while(in.LA(1) != CharInput.EOF) {
			head = new Node(head, in.mark());
			//m = in.mark();
			if(!expr.accept(in)) {
				//in.rewind(m);
				in.rewind(head.payload);
				head = head.next;
				break;
			}
		}

		// now let's check if the next expression matches, too
		if(!next[thisOne+1].accept(in)) {
			
		}
		return true;
	}*/

	public String toString() { return expr.toString() + "+"; }

	private static class Node {
		int payload;
		Node next;

		public Node(Node next, int payload) {
			this.next = next;
			this.payload = payload;
		}
	}
}
