package net.yajjl;

import java.math.BigDecimal;

public class JsonLexer {
	private Token<?> currentToken;
	private int offset;
	private int beginLexeme, endLexeme;
	
	private String input;
	
	public static final Token<Boolean> BOOLEAN = new Token<Boolean>("<Boolean>") {
		@Override
		public Boolean evaluate(String lexeme) {
			return Boolean.parseBoolean(lexeme.toString());
		}
	};

	public static final Token<Number> NUMBER = new Token<Number>("<Number>") {
		@Override
		public Number evaluate(String lexeme) {
			return new BigDecimal(lexeme);
		}
	};

	public static final Token<String> STRING = new Token<String>("<String>") { 
		@Override
		public String evaluate(String lexeme) {
			return lexeme.substring(1, lexeme.length()-1);
		}
	};

	public static final Token<?> NULL = new NullToken("'null'");
	public static final Token<?> OPEN_BRACKET = new NullToken("'['");
	public static final Token<?> CLOSE_BRACKET = new NullToken("']'");
	public static final Token<?> OPEN_BRACE = new NullToken("'{'");
	public static final Token<?> CLOSE_BRACE = new NullToken("'}'");
	public static final Token<?> COMMA = new NullToken("','");
	public static final Token<?> COLON = new NullToken("':'");
	
	public static final Token<?>[] VALUE_START_TOKENS = new Token[] { NULL, BOOLEAN, NUMBER, STRING, OPEN_BRACKET, OPEN_BRACE };
	

	public JsonLexer(String input) {
		reset(input);
	}
	
	public void reset(String input) {
		this.input = input;
		this.beginLexeme = 0;
		this.endLexeme = 0;
		this.offset = 0;
		this.currentToken = null;
	}

	public boolean moveNext() {
		eatWhitespace();
		
		return
			eatOpenBrace() || 
			eatCloseBrace() ||
			eatOpenBracket() ||
			eatCloseBracket() ||
			eatComma() ||
			eatColon() ||
			eatNull() ||
			eatBoolean() ||
			eatNumber() ||
			eatString(); 
	}

	public String getLexeme() { 
		return input.substring(beginLexeme, endLexeme).trim(); 
	}

	public Token<?> getToken() { 
		return currentToken; 
	}
	
	public long getOffset() { 
		return offset; 
	}
	
	public Object getValue() {
		return getToken().evaluate(getLexeme());
	}

	private boolean eatOpenBrace() { return eatToken('{', OPEN_BRACE); }
	private boolean eatCloseBrace() { return eatToken('}', CLOSE_BRACE); }
	private boolean eatOpenBracket() { return eatToken('[', OPEN_BRACKET); }
	private boolean eatCloseBracket() { return eatToken(']', CLOSE_BRACKET); }
	private boolean eatComma() { return eatToken(',', COMMA); }
	private boolean eatColon() { return eatToken(':', COLON); }

	private boolean eatNull() {
		eatWhitespace();
		beginLexeme();
		
		if ( !eat("null") ) return false;
		
		endLexeme(NULL);	
		return true;
	}

	private boolean eatBoolean() { 
		eatWhitespace();	
		beginLexeme();
		
		if ( !eat("true") && !eat("false") ) return false;
		
		endLexeme(BOOLEAN);
		return true;
	}

	private boolean eatNumber() {
		eatWhitespace();		
		beginLexeme();

		eat('-');
		
		if (!eat(DEC_DIGITS)) return false;
		while (eat(DEC_DIGITS)) ; // keep eating decimal digits
		
		if (eat('.')) {
			if (!eat(DEC_DIGITS)) return false; 
			while (eat(DEC_DIGITS)) ; // keep eating decimal digits
		}
		
		if (eat('e', 'E')) {
			eat('-', '+');
			
			if (!eat(DEC_DIGITS)) return false; 
			while (eat(DEC_DIGITS)) ;
		}
		
		endLexeme(NUMBER);		
		return true;
	}

	private boolean eatString() {
		eatWhitespace();		
		beginLexeme();

		if (!eat('"')) return false;

		while (!eat('"')) {
			if (finished()) return false;
			if (eat('\\')) {
				if (eat('"', '\\', '/', 'b', 'f', 'n', 'r', 't')) continue;
				if (eat('u') && 
					eat(HEX_DIGITS) &&
					eat(HEX_DIGITS) &&
					eat(HEX_DIGITS) &&
					eat(HEX_DIGITS) ) continue;
				
				// there's a problem
				return false;
			}
			offset += 1;
		}
		
		endLexeme(STRING);
		return true;
	}

	private boolean eatToken(char expected, Token<?> token) {
		eatWhitespace();
		beginLexeme();
		
		if (!eat(expected)) return false;
		
		endLexeme(token);
		return true;
	}

	// eat the whole of expected
	private boolean eat(String expected) {
		if (expected.length() > (input.length()-offset)) return false;
		
		for (int i = 0; i < expected.length(); i++) {
			if (input.charAt(offset+i) != expected.charAt(i)) {
				return false;
			}
		}
		
		this.offset += expected.length();
		return true;
	}

	// eat any of the expected
	private boolean eat(char... expected) {
		if (finished()) return false;
		
		for (char c : expected) {
			if (input.charAt(offset) == c) {
				this.offset += 1;
				return true;
			}
		}
		
		return false;
	}
	
	private void eatWhitespace() {
		while (!finished() && Character.isWhitespace(input.charAt(offset))) offset += 1;
	}

	private void beginLexeme() { 
		this.beginLexeme = this.offset; 
		this.endLexeme = this.offset; 
	}
	
	private void endLexeme(Token<?> token) { 
		this.endLexeme = offset; 
		this.currentToken = token; 
	}
	
	private boolean finished() { 
		return offset >= input.length(); 
	}

	private static final char[] DEC_DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	private static final char[] HEX_DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	public abstract static class Token<T> {
		private final String name;
		
		public Token(String name) { this.name = name; }
	
		// override this for tokens which represent a value
		public abstract T evaluate(String lexeme);
		
		@Override
		public String toString() { return name; }
	}
	
	public static class NullToken extends Token<Object> {

		public NullToken(String name) {
			super(name);
		}

		@Override
		public Object evaluate(String lexeme) { return null; }
	}
	
}