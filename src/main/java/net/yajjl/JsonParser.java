package net.yajjl;

import static net.yajjl.JsonLexer.BOOLEAN;
import static net.yajjl.JsonLexer.CLOSE_BRACE;
import static net.yajjl.JsonLexer.CLOSE_BRACKET;
import static net.yajjl.JsonLexer.COLON;
import static net.yajjl.JsonLexer.COMMA;
import static net.yajjl.JsonLexer.NULL;
import static net.yajjl.JsonLexer.NUMBER;
import static net.yajjl.JsonLexer.OPEN_BRACE;
import static net.yajjl.JsonLexer.OPEN_BRACKET;
import static net.yajjl.JsonLexer.STRING;
import static net.yajjl.JsonLexer.VALUE_START_TOKENS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.yajjl.JsonLexer.Token;

public class JsonParser {
	private Object readNull(JsonLexer lexer) throws ParseException {
		expectCurrentToken(lexer, NULL);
		lexer.moveNext();
		return null;
	}

	private Boolean readBoolean(JsonLexer lexer) throws ParseException {
		expectCurrentToken(lexer, BOOLEAN);
		Boolean bool = (Boolean) lexer.getValue();
		lexer.moveNext();
		return bool;
	}

	private Number readNumber(JsonLexer lexer) throws ParseException {
		expectCurrentToken(lexer, NUMBER);
		Number num = (Number) lexer.getValue();
		lexer.moveNext();
		return new BigDecimal(num.toString());
	}

	private String readString(JsonLexer lexer) throws ParseException {
		expectCurrentToken(lexer, STRING);
		String str = unescape((String) lexer.getValue());
		lexer.moveNext();
		return str.toString();
	}

	private String unescape(String value) {
		StringBuffer buffer = new StringBuffer();
		
		boolean escaping = false;
		
		for (int i = 0; i < value.length(); i += 1) {
			char c = value.charAt(i);
		
			if (escaping) {
				switch (c) {
				case '"':
					buffer.append('"');
					break;
				case '\\':
					buffer.append('\\');
					break;
				case '/':
					buffer.append('/');
					break;
				case 'b':
					buffer.append('\b');
					break;
				case 'f':
					buffer.append('\f');
					break;
				case 'n':
					buffer.append('\n');
					break;
				case 'r':
					buffer.append('\r');
					break;
				case 't':
					buffer.append('\t');
					break;
				case 'u':
					// interpret the following 4 characters as the hex of the unicode code point
					int codePoint = Integer.parseInt(value.substring(i + 1, i + 5), 16);
					buffer.appendCodePoint(codePoint);
					i += 4;
					break;
				default:
					throw new IllegalArgumentException("Illegal escape sequence: '\\" + c + "'");
				}
				escaping = false;
			} else {
				if (c == '\\') {
					escaping = true;
				} else {
					buffer.append(c);
				}
			}
		}
		
		return buffer.toString();
	}

	private Map<String, Object> readObject(JsonLexer lexer) throws ParseException {
		expectCurrentToken(lexer, OPEN_BRACE);
		expectMoveNext(lexer, STRING, CLOSE_BRACE);
		Map<String, Object> object = new HashMap<String, Object>();

		while (isCurrentToken(lexer, STRING)) {
			String string = readString(lexer);
			expectCurrentToken(lexer,COLON);
			expectMoveNext(lexer, VALUE_START_TOKENS);
			Object value = readValue(lexer);
			object.put(string, value);
			if (isCurrentToken(lexer, CLOSE_BRACE))
				break;
			else {
				expectCurrentToken(lexer,COMMA);
				expectMoveNext(lexer, VALUE_START_TOKENS);
			}
		}

		expectCurrentToken(lexer,CLOSE_BRACE);
		lexer.moveNext();

		return object;
	}

	private List<?> readArray(JsonLexer lexer) throws ParseException {
		expectCurrentToken(lexer,OPEN_BRACKET);
		expectMoveNext(lexer, NULL, BOOLEAN, NUMBER, STRING, OPEN_BRACKET, OPEN_BRACE, CLOSE_BRACKET);

		List<Object> array = new ArrayList<Object>();

		while (isCurrentToken(lexer, VALUE_START_TOKENS)) {
			array.add(readValue(lexer));

			if (isCurrentToken(lexer, CLOSE_BRACKET)) {
				break;
			} else {
				expectCurrentToken(lexer,COMMA);
				expectMoveNext(lexer, VALUE_START_TOKENS);
			}
		}

		expectCurrentToken(lexer,CLOSE_BRACKET);
		lexer.moveNext();

		return array;
	}

	private Object readValue(JsonLexer lexer) throws ParseException {
		if (isCurrentToken(lexer, NULL)) {
			return readNull(lexer);
		} else if (isCurrentToken(lexer, BOOLEAN)) {
			return readBoolean(lexer);
		} else if (isCurrentToken(lexer, NUMBER)) {
			return readNumber(lexer);
		} else if (isCurrentToken(lexer, STRING)) {
			return readString(lexer);
		} else if (isCurrentToken(lexer, OPEN_BRACKET)) {
			return readArray(lexer);
		} else if (isCurrentToken(lexer, OPEN_BRACE)) {
			return readObject(lexer);
		} else {
			throw new ParseException(lexer.getLexeme(), VALUE_START_TOKENS);
		}
	}

	/**
	 * Checks that the current token is one of <tt>expected</tt>, and if not throws a ParseException
	 */
	private void expectCurrentToken(JsonLexer lexer, Token... expected) throws ParseException {
		if (!isCurrentToken(lexer, expected)) {
			throw new ParseException(lexer.getLexeme(), expected);
		}
	}

	/**
	 * Attempts to move the lexer to the next token, and throws a ParseException with the expected next token types if not successful
	 */
	private void expectMoveNext(JsonLexer lexer, Token... expected) throws ParseException {
		if (!lexer.moveNext()) {
			throw new ParseException("no valid tokens from " + lexer.getOffset(), expected);
		}
	}

	private boolean isCurrentToken(JsonLexer lexer, Token... toks) {
		for (Token tok : toks) {
			if (lexer.getToken().equals(tok))
				return true;
		}
		return false;
	}

	public static class ParseException extends Exception {

		private ParseException(String message) {
			super(message);
		}

		private ParseException(String found, Token... expected) {
			super(buildMessage(found, expected));
		}

		private static String buildMessage(String found, Token... expected) {
			StringBuffer buffer = new StringBuffer("Expected: ");

			for (int i = 0; i < expected.length; i++) {
				buffer.append(expected[i].toString());
				if (i < expected.length - 1)
					buffer.append(" or ");
			}

			buffer.append(" , Found: '" + found + "'");
			
			return buffer.toString();
		}
	}

	public Object parseNull(String json) throws ParseException {
		readNull(initLexer(json, NULL));
		return null;
	}

	public Boolean parseBoolean(String json) throws ParseException {
		return readBoolean(initLexer(json, BOOLEAN));
	}
	
	public Number parseNumber(String json) throws ParseException {
		return readNumber(initLexer(json, NUMBER));
	}

	public String parseString(String json) throws ParseException {
		return readString(initLexer(json, STRING));
	}

	public List<?> parseArray(String json) throws ParseException {
		return readArray(initLexer(json, OPEN_BRACKET));
	}

	public Map<String, Object> parseObject(String json) throws ParseException {
		return readObject(initLexer(json, OPEN_BRACE));
	}

	private JsonLexer initLexer(String json, Token firstToken) throws ParseException {
		JsonLexer lexer = new JsonLexer(json);
		expectMoveNext(lexer, firstToken);
		return lexer;
	}
}
