package com.pagosoft.expression;

public class CharInput {
	public static final int EOF = -1;

	private char[] input;
	private int pos, end;

	public CharInput(char[] input) {
		this(input, 0, input.length);
	}

	public CharInput(char[] input, int pos, int end) {
		this.end = end;
		this.input = input;
		this.pos = pos;
	}

	public int LA(int k) {
		if(k == 0) return 0;
		if(k < 0) {
			k++;
			if(pos+k-1 < 0) {
				return EOF;
			}
		}
		if(pos+k-1 >= end) {
			return EOF;
		}
		return input[pos+k-1];
	}

	public void consume() {
		pos++;
	}

	/**
	 * consumes every character until it finds the selected one
	 * @param c
	 */
	public void consumeUntil(char c) {
		while(pos < end && input[pos] != c) pos++;
		if(LA(1) == c) pos++; // consume the last one, too
	}

	public void consumeWhitespace() {
		while(Character.isWhitespace(LA(1))) pos++;
	}

	public int getPosition() {
		return pos;
	}

	public int getEndPosition() {
		return end;
	}

	public int mark() {
		return pos;
	}

	public void rewind(int mark) {
		pos = mark;
	}

	public String substring(int offset, int count) {
		return new String(input, offset, count);
	}

	/**
	 * Will try to match the specified input from the current buffer position and consume it at the same time.
	 * It'll rewind if the input cannot be matched.
	 * @param test
	 * @return true on success (when input was consumed), false otherwise (input was not consumed, position rewinded)
	 */
	public boolean match(CharSequence test) {
		// that'd be an opportunity to introduce mark/rewind-feature...
		int m = mark();
		for(int i = 0; i < test.length(); i++) {
			if(pos >= end || input[pos] != test.charAt(i)) {
				rewind(m);
				return false;
			}
			pos++;
		}
		return true;
	}

	public boolean match(char[] test) {
		// that'd be an opportunity to introduce mark/rewind-feature...
		int m = mark();
		for(int i = 0; i < test.length; i++) {
			if(pos >= end || input[pos] != test[i]) {
				rewind(m);
				return false;
			}
			pos++;
		}
		return true;
	}

	public boolean match(char test) {
		if(LA(1) == test) {
			pos++;
			return true;
		}
		return false;
	}

	public int findNextOf(char ... tests) {
		int m = mark();
		while(true) {
			int c = LA(1);
			if(c == EOF) break;
			for(char t: tests) {
				if(c == t) {
					rewind(m);
					return t;
				}
			}
		}
		rewind(m);
		return EOF;
	}
}
