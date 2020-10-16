package com.pagosoft.expression;

import java.util.List;
import java.util.ArrayList;

public abstract class Expression {
	/**
	 * Proceeds behind the current expression (and returns true) or does not move (and returns false)
	 * if the next symbol doesn't match this expression.
	 * @param in
	 * @return
	 */
	public abstract boolean accept(CharInput in);

	public boolean accept(String in) {
		return accept(new CharInput(in.toCharArray()));
	}

	public boolean accept(CharSequence in) {
		return accept(new CharInput(sequenceToArray(in)));
	}

	public String match(CharInput in) {
		int m = in.mark();
		if(accept(in)) {
			return in.substring(m, in.getPosition()-m);
		}
		return null;
	}

	public Expression union(Expression ... exprs) {
		Expression[] union = new Expression[exprs.length+1];
		for(int i = 0; i < union.length; i++) {
			if(i == 0) union[i] = this;
			else union[i] = exprs[i-1];
		}
		return new OrExpression(union);
	}

	public Expression repeat() {
		return new RepeatedExpression(this);
	}

	public Expression optional() {
		return new OptionalExpression(this);
	}

	public Expression repeat(int min) {
		return repeat(min, -1);
	}

	public Expression repeat(int min, int max) {
		return new LimitedRepeatExpression(this, min, max);
	}

	public Expression concat(Expression ... exprs) {
		Expression[] union = new Expression[exprs.length+1];
		for(int i = 0; i < union.length; i++) {
			if(i == 0) union[i] = this;
			else union[i] = exprs[i-1];
		}
		return new GroupExpression(union);
	}

	static char[] sequenceToArray(CharSequence input) {
		char[] in = new char[input.length()];
		for(int i = 0; i < in.length; i++) in[i] = input.charAt(i);
		return in;
	}

	public static Expression compile(CharSequence input) {
		// anyway, start parsing
		CharInput charin = new CharInput(sequenceToArray(input));
		ExpressionParser parser = new ExpressionParser(charin);
		List<Expression> exprs = new ArrayList<Expression>();
		while(charin.LA(1) != CharInput.EOF) {
			Expression expr = parser.match();
			if(expr == NO_EXPRESSION) {
				// better error handling?
				throw new IllegalArgumentException("expression cannot be compiled without errors");
			}
			exprs.add(expr);
		}
		if(exprs.size() == 1) return exprs.get(0);
		return new GroupExpression(exprs.toArray(new Expression[exprs.size()]));
	}

	// doesn't accept anything
	private final static Expression NO_EXPRESSION = new Expression() {
		public boolean accept(CharInput in) {
			return false;
		}
	};

	private static class ExpressionParser {
		private CharInput input;

		public ExpressionParser(CharInput input) {
			this.input = input;
		}

		public Expression match() {
			int c = input.LA(1);
			Expression result = NO_EXPRESSION;
			if(c == CharInput.EOF) return result;

			if('.' == c) {
				input.consume();
				result = AnySymbolExpression.INSTANCE;
			}

			if('$' == c) {
				input.consume();
				result = EndExpression.INSTANCE;
			}

			if('^' == c) {
				input.consume();
				result = StartExpression.INSTANCE;
			}

			// check if it is a predicate expression
			Predicate predicate = Predicate.NONE;
			if(input.LA(2) == '(') {
			    if(c == '&') {
				predicate = Predicate.TRUE;
			    } else if(c == '!') {
				predicate = Predicate.FALSE;
			    }

			    if(predicate != Predicate.NONE) {
				input.consume();
				c = input.LA(1);
			    }
			}

			// it might be a group of expressions
			if('(' == c) {
				input.consume();
				List<Expression> exprs = new ArrayList<Expression>();
				while(input.LA(1) != ')') {
					exprs.add(match());
				}
				input.consume();
				if(exprs.size() == 1) result = exprs.get(0);
				else result = new GroupExpression(exprs.toArray(new Expression[exprs.size()]));

				// wrap in predicate (if any)
				result = predicate.wrap(result);
			}

			// character class?
			if('[' == c) {
				input.consume();
				boolean invert = false;
				if(input.LA(1) == '^') {
					input.consume();
					invert = true;
				}
				// every character stands for it's own account
				List<Expression> exprs = new ArrayList<Expression>();
				while(input.LA(1) != ']') {
					int current = input.LA(1);
					input.consume();
					// just for savety
					if(current == CharInput.EOF) return result;

					// now the real work
					// escaped characters
					if(current == '\\') {
						current = input.LA(1);
						input.consume();
						// again: safety first
						if(current == CharInput.EOF) return result;
						// maybe we got our expression already
						Expression expr = specialGroup(current);
						if(expr != NO_EXPRESSION) {
							exprs.add(expr);
							continue;
						}
					}

					// is it a range?
					if(input.LA(1) == '-') {
						input.consume(); // throw away the "-"
						int rangeEnd = input.LA(1);
						input.consume();
						if(rangeEnd == '\\') {
							rangeEnd = input.LA(1);
							input.consume();
							// savety first
							if(rangeEnd == CharInput.EOF) return result;
						}
						exprs.add(new RangeExpression((char)current, (char)rangeEnd));
					} else {
						exprs.add(new CharExpression((char)current));
					}
				}
				input.consume(); // consume ']'
				if(invert) result = new NotExpression(exprs.toArray(new Expression[exprs.size()]));
				else       result = new OrExpression(exprs.toArray(new Expression[exprs.size()]));
			}

			if('\\' == c && (result = specialGroup(input.LA(2))) != NO_EXPRESSION) {
				input.consume();
				input.consume();
			}

			// it is a symbol (any special character at this position would be illegal)
			if(result == NO_EXPRESSION) {
				input.consume();

				// special case (optimization):
				{
					int next = input.LA(1);
					if(c == '\\' && next == '\\') {
						int t = input.LA(2);
						if(t == '?' || t == '*' || t == '+') {
							input.consume(); // consume \
							next = t;
						}
					}
					if(next == '?') {
						result = new OptionalExpression(new CharExpression((char)c));
						input.consume();
					} else if(next == '*') {
						result = new OptionalExpression(new RepeatedExpression(new CharExpression((char)c)));
						input.consume();
					} else if(next == '+') {
						result = new RepeatedExpression(new CharExpression((char)c));
						input.consume();
					}
				}

				if(result == NO_EXPRESSION) {
					StringBuilder builder = new StringBuilder();
					builder.append((char)c);
					int current;
					while(!isSpecial(current = input.LA(1))) {
						// check if the character after this one is a quantifier
						if(current != '\\') {
							int next = input.LA(2);
							if(next == '?' || next == '*' || next == '+') {
								break;
							}
						} else { // current == \
							int m = input.mark();
							input.consume();
							current = input.LA(1);

							// special case: \\
							if(current == '\\') {
								input.consume();
								int next = input.LA(2);
								// special special case: \\+, \\*, \\?
								if(next == '?' || next == '*' || next == '+') {
									input.rewind(m);
									break;
								}
								builder.append((char)current);
								continue;
							} else {
								// if there is a special group expression for this character
								// we need to stop here so we can recognize it as such in the next step
								Expression expr = specialGroup(current);
								if(expr != NO_EXPRESSION) {
									input.rewind(m);
									break;
								}
							}
						}

						input.consume();
						builder.append((char)current);
					}
					result = new SymbolExpression(builder);
				}
			}

			// any quantifiers/modifiers?
			if(result != NO_EXPRESSION) {
				c = input.LA(1);
				switch(c) {
					case '?': input.consume(); result = new OptionalExpression(result); break;
					case '+':
						input.consume();
						result = new RepeatedExpression(result);
						break;
					case '*':
						input.consume();
						result = new OptionalExpression(new RepeatedExpression(result));
						break;
					case '{':
						input.consume();
						int min = matchNumber();
						if(input.LA(1) == ',') {
							input.consume();
							if(input.LA(1) == '}') {
								// means: at least min times
								result = new LimitedRepeatExpression(result, min, -1);
							} else {
								// between min and max times
								result = new LimitedRepeatExpression(result, min, matchNumber());
							}
						} else {
							// exactly min-times
							result = new LimitedRepeatExpression(result, min, min);
						}
						input.consume();
						break;
				}
				// special case because this must be handled even if there was another modifier before it.
				if(input.LA(1) == '|') {
					input.consume();
					result = new OrExpression(new Expression[] {result, match()});
				}
			}

			return result;
		}

		private boolean isSpecial(int i) {
			return i == '[' || i == '(' || i == ')' || i == ']' || i == '?' || i == '+' || i == '*' || i == '.'
					|| i == '|' || i == '$' || i == '{' || i == '}' || i == CharInput.EOF;
		}

		private int matchNumber() {
			// kill whitespace
			while(Character.isWhitespace(input.LA(1))) input.consume();

			// fetch number
			StringBuilder builder = new StringBuilder();
			while(true) {
				int c = input.LA(1);
				if(c != CharInput.EOF && Character.isDigit((char)c)) {
					builder.append((char)c);
					input.consume();
				} else {
					break;
				}
			}
			// kill whitespace
			while(Character.isWhitespace(input.LA(1))) input.consume();

			// return result
			return Integer.parseInt(builder.toString());
		}

		private Expression specialGroup(int c) {
			switch(c) {
				case 's': return WHITESPACE_EXPR;
				case 'S': return NO_WHITESPACE_EXPR;
				case 'd': return DIGIT_EXPR;
				case 'D': return NO_DIGIT_EXPR;
				case 'w': return WORD_EXPR;
				case 'W': return NO_WORD_EXPR;
				case 'x': return HEXDIGIT_EXPR;
				default: return NO_EXPRESSION;
			}
		}

		private static final Expression WHITESPACE_EXPR = new Expression() {
			public boolean accept(CharInput in) {
				if(Character.isWhitespace(in.LA(1))) {
					in.consume();
					return true;
				}
				return false;
			}
		};

		private static final Expression NO_WHITESPACE_EXPR = new Expression() {
			public boolean accept(CharInput in) {
				if(!Character.isWhitespace(in.LA(1))) {
					in.consume();
					return true;
				}
				return false;
			}
		};

		private static final Expression DIGIT_EXPR = new Expression() {
			public boolean accept(CharInput in) {
				if(Character.isDigit(in.LA(1))) {
					in.consume();
					return true;
				}
				return false;
			}
		};

		private static final Expression NO_DIGIT_EXPR = new Expression() {
			public boolean accept(CharInput in) {
				if(!Character.isDigit(in.LA(1))) {
					in.consume();
					return true;
				}
				return false;
			}
		};

		private static final Expression WORD_EXPR = new Expression() {
			public boolean accept(CharInput in) {
				if(Character.isLetter(in.LA(1))) {
					in.consume();
					return true;
				}
				return false;
			}
		};

		private static final Expression NO_WORD_EXPR = new Expression() {
			public boolean accept(CharInput in) {
				if(!Character.isLetter(in.LA(1))) {
					in.consume();
					return true;
				}
				return false;
			}
		};

		private static final Expression HEXDIGIT_EXPR = new Expression() {
			public boolean accept(CharInput in) {
				int c = in.LA(1);
				if(Character.isDigit(c) || ('A' <= c && c <= 'F') || ('a' <= c && c <= 'f')) {
					in.consume();
					return true;
				}
				return false;
			}
		};
	}
}
