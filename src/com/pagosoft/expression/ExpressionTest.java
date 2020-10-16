package com.pagosoft.expression;

import java.util.regex.Pattern;
import static com.pagosoft.expression.Expression.compile;

public class ExpressionTest {
	private static final Expression EXPR = Expression.compile("[^t]");
	public static void main3(String args[]) {
		for(int i = 0; i < 10; i++) {
			long t = System.nanoTime();
			test(EXPR, "es");
			//EXPR.accept("hello world");
			t = System.nanoTime() - t;
			System.out.println(t);
		}
		// test java-api
		// [0-9]*(\\.[0-9]+[fF]?)?
		Expression digit = Expression.compile("[0-9]");
		Expression number = digit.repeat().optional().concat(
				compile("\\."), digit.repeat(), compile("[fF]").optional());
	}

	public static void main(String[] args) {
	    String comment = "\"hello \\\"world!\"";
	    // first: verify that a simple greedy expression can't handle this
	    boolean result = Expression.compile("\".*\"").accept(comment);
	    System.out.printf("Simple expression: %s (expected: false)%n", result);

	    Expression commentExpr =
		    new CharExpression('"').concat(
			new CharExpression('\\').concat(
			    new AnySymbolExpression()
			).union(
			    new PredicateExpression(new CharExpression('"'), false).concat(
				new AnySymbolExpression()
			    )
			).repeat(),
		    new CharExpression('"'));
	    result = commentExpr.accept(comment);
	    System.out.printf("Manual predicate expression: %s (expected: true)%n", result);

	    result = Expression.compile("\"(\\\\.|!(\").)*\"").accept(comment);
	    System.out.printf("Predicate expression: %s (expected: true)%n", result);
	}
	
	public static void test(Expression expr, String test) {
		for(int i = 0; i < 10; i++)
			expr.accept(test);
	}
	
	public static void main2(String[] args) {
		//warmup-phase
		final String pattern = "^hello *(world|universe)[0-9]?!{0,}$";
		final String input = "hello world!";
		for(int i = 0; i < 100; i++)
			time(Pattern.compile(pattern), input, true);

		for(int i = 0; i < 100; i++)
			time(Expression.compile(pattern), input, true);

		// very simple regex
		time("hello world", "hello world", true);

		// test quantifiers
		time("hello +world", "hello                                                               world", true);
		time("hello\\s+world", "hello world", true);
		time("hello\\s*world", "hello                           world", true);
		time("hello ?world", "hello world", true);
		time("hello ?world", "helloworld", true);

		// test or
		time("universe|world", "world", true);
		time("universe|world", "universe", true);

		// test groups
		time("[01]", "0", true);
		time("[01]", "1", true);

		// test ranges
		time("[0-9]", "0", true);
		time("[0-9]", "5", true);

		// test limited repetitions
		time("1{3}", "111", true);
		time("1{3,}", "11111", true);
		time("1{1,3}", "11", true);

		// floating point number (or integer)
		time("[0-9]*(\\.[0-9]+[fF]?)?", "15.21", true);

		// that one was a stupid problem before.
		time("a{3}|b{3}", "aaa", true);

		// this one is expected to fail using this library but should work using Suns engine.
		time(".*ba", "fooba", true);

		// ending stuff
		time("[^t]", "es", true);
		time("[^\\)]+", "test", true);

		// fun thing
		final Expression INTEGER = Expression.compile("[0-9]+");
		final Expression WS = Expression.compile(" *");
		CharInput in = new CharInput("{5,42}".toCharArray());
		if(in.LA(1) == '{') {
			in.consume();
			WS.accept(in);
			String min = INTEGER.match(in), max = null;
			WS.accept(in);
			if(in.LA(1) == ',') {
				in.consume();
				WS.accept(in);
				if(in.LA(1) != '}') {
					max = INTEGER.match(in);
					WS.accept(in);
				}
				in.consume(); // consume the }
			}
			// we already know we matched both ;)
			System.out.printf("min = %s, max = %s%n", min, max);
		}


		// measure compile time
		System.out.println("");

		long start = System.nanoTime();
		for(int i = 0; i < 1000; i++) Pattern.compile(pattern);
		System.out.println("JDK Compile time: "+(System.nanoTime()-start));
		
		start = System.nanoTime();
		for(int i = 0; i < 1000; i++) Expression.compile(pattern);
		System.out.println("PGS Compile time: "+(System.nanoTime()-start));//*/
	}

	public static void time(String regex, String input, boolean expected) {
		System.out.println("Regex: "+regex);
		System.out.println("Input: "+input);

		// compile the regex
		Expression expr = Expression.compile(regex);
		Pattern pattern = Pattern.compile(regex);

		System.out.println("JDK Regex: "+time(pattern, input, expected)+"ns");
		System.out.println("PGS Regex: "+time(expr, input, expected)+"ns");
		System.out.println("");
	}

	private static final int REPEAT = 1000;

	public static long time(Pattern pattern, String input, boolean expected) {
		long start = System.nanoTime();
		for(int i = 0; i < REPEAT; i++) {
			if(pattern.matcher(input).matches() != expected) {
				System.out.println("Regex did not return the expected result (expected: "+expected+")");
				break;
			}
		}
		return System.nanoTime() - start;
	}

	public static long time(Expression expr, String input, boolean expected) {
		long start = System.nanoTime();
		for(int i = 0; i < REPEAT; i++) {
			if(expr.accept(input) != expected) {
				System.out.println("Regex did not return the expected result (expected: "+expected+")");
				break;
			}
		}
		return System.nanoTime() - start;
	}
}
