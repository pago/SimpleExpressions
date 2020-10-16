package com.pagosoft.expression;

/**
 *
 * @author pago
 */
public class PredicateExpression extends Expression {
    private Expression delegate;
    private boolean test;

    public PredicateExpression(Expression delegate) {
	this(delegate, true);
    }

    public PredicateExpression(Expression delegate, boolean test) {
	this.delegate = delegate;
	this.test = test;
    }

    @Override
    public boolean accept(CharInput in) {
	int m = in.mark();
	boolean result = delegate.accept(in);
	in.rewind(m);
	return result == test;
    }
}
