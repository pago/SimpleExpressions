package com.pagosoft.expression;

/**
 *
 * @author pago
 */
public enum Predicate {
    TRUE {
	@Override
	Expression wrap(Expression expr) {
	    return new PredicateExpression(expr, true);
	}
    },
    FALSE {
	@Override
	Expression wrap(Expression expr) {
	    return new PredicateExpression(expr, false);
	}
    },
    NONE {
	@Override
	Expression wrap(Expression expr) {
	    return expr;
	}
    }
    ;
    abstract Expression wrap(Expression expr);
}
