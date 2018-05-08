package br.com.brandizzi.adam.myjlox;

import static br.com.brandizzi.adam.myjlox.TokenType.AND;
import static br.com.brandizzi.adam.myjlox.TokenType.BANG;
import static br.com.brandizzi.adam.myjlox.TokenType.BANG_EQUAL;
import static br.com.brandizzi.adam.myjlox.TokenType.BREAK;
import static br.com.brandizzi.adam.myjlox.TokenType.CLASS;
import static br.com.brandizzi.adam.myjlox.TokenType.COLON;
import static br.com.brandizzi.adam.myjlox.TokenType.COMMA;
import static br.com.brandizzi.adam.myjlox.TokenType.DOT;
import static br.com.brandizzi.adam.myjlox.TokenType.ELSE;
import static br.com.brandizzi.adam.myjlox.TokenType.EOF;
import static br.com.brandizzi.adam.myjlox.TokenType.EQUAL;
import static br.com.brandizzi.adam.myjlox.TokenType.EQUAL_EQUAL;
import static br.com.brandizzi.adam.myjlox.TokenType.FALSE;
import static br.com.brandizzi.adam.myjlox.TokenType.FOR;
import static br.com.brandizzi.adam.myjlox.TokenType.FUN;
import static br.com.brandizzi.adam.myjlox.TokenType.GREATER;
import static br.com.brandizzi.adam.myjlox.TokenType.GREATER_EQUAL;
import static br.com.brandizzi.adam.myjlox.TokenType.IDENTIFIER;
import static br.com.brandizzi.adam.myjlox.TokenType.IF;
import static br.com.brandizzi.adam.myjlox.TokenType.LEFT_BRACE;
import static br.com.brandizzi.adam.myjlox.TokenType.LEFT_PAREN;
import static br.com.brandizzi.adam.myjlox.TokenType.LESS;
import static br.com.brandizzi.adam.myjlox.TokenType.LESS_EQUAL;
import static br.com.brandizzi.adam.myjlox.TokenType.MINUS;
import static br.com.brandizzi.adam.myjlox.TokenType.NIL;
import static br.com.brandizzi.adam.myjlox.TokenType.NUMBER;
import static br.com.brandizzi.adam.myjlox.TokenType.OR;
import static br.com.brandizzi.adam.myjlox.TokenType.PLUS;
import static br.com.brandizzi.adam.myjlox.TokenType.PRINT;
import static br.com.brandizzi.adam.myjlox.TokenType.QUESTION;
import static br.com.brandizzi.adam.myjlox.TokenType.RETURN;
import static br.com.brandizzi.adam.myjlox.TokenType.RIGHT_BRACE;
import static br.com.brandizzi.adam.myjlox.TokenType.RIGHT_PAREN;
import static br.com.brandizzi.adam.myjlox.TokenType.SEMICOLON;
import static br.com.brandizzi.adam.myjlox.TokenType.SLASH;
import static br.com.brandizzi.adam.myjlox.TokenType.STAR;
import static br.com.brandizzi.adam.myjlox.TokenType.STRING;
import static br.com.brandizzi.adam.myjlox.TokenType.SUPER;
import static br.com.brandizzi.adam.myjlox.TokenType.THIS;
import static br.com.brandizzi.adam.myjlox.TokenType.TRUE;
import static br.com.brandizzi.adam.myjlox.TokenType.VAR;
import static br.com.brandizzi.adam.myjlox.TokenType.WHILE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	private int start = 0;
	private int current = 0;
	private int line = 1;
	private static final Map<String, TokenType> keywords;

	public Scanner(String source) {
		this.source = source;
	}

	public List<Token> scanTokens() {
		while (!isAtEnd()) {
			// We are at the beginning of the next lexeme.
			start = current;
			scanToken();
		}

		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	private boolean isAtEnd() {
		return current >= source.length();
	}

	private void scanToken() {
		char c = advance();
		switch (c) {
		case '(':
			addToken(LEFT_PAREN);
			break;
		case ')':
			addToken(RIGHT_PAREN);
			break;
		case '{':
			addToken(LEFT_BRACE);
			break;
		case '}':
			addToken(RIGHT_BRACE);
			break;
		case ',':
			addToken(COMMA);
			break;
		case '.':
			addToken(DOT);
			break;
		case '-':
			addToken(MINUS);
			break;
		case '+':
			addToken(PLUS);
			break;
		case ';':
			addToken(SEMICOLON);
			break;
		case '*':
			addToken(STAR);
			break;
		case '!':
			addToken(match('=') ? BANG_EQUAL : BANG);
			break;
		case '=':
			addToken(match('=') ? EQUAL_EQUAL : EQUAL);
			break;
		case '<':
			addToken(match('=') ? LESS_EQUAL : LESS);
			break;
		case '>':
			addToken(match('=') ? GREATER_EQUAL : GREATER);
			break;
		case '?':
			addToken(QUESTION);
			break;
		case ':':
			addToken(COLON);
			break;
		case '/':
			if (match('/')) {
				consumeLineComment();
			} else if (match('*')) {
				consumeMultilineComment();
			} else {
				addToken(SLASH);
			}
			break;
		case ' ':
		case '\r':
		case '\t':
			// Ignore whitespace.
			break;

		case '\n':
			line++;
			break;
		case '"':
			string();
			break;
		default:
			if (isDigit(c)) {
				number();
			} else if (isAlpha(c)) {
				identifier();
			} else {
				Lox.error(line, "Unexpected character.");
			}
			break;
		}
	}

	private void consumeMultilineComment() {
		while ((peek() != '*' || peekNext() != '/') && !isAtEnd()) {
			if ((peek() == '/' && peekNext() == '*')) {
				drop('/');
				drop('*');
				consumeMultilineComment();
			}
			char character = advance();

			if (character == '\n') {
				line++;
			}
		}
		if (isAtEnd()) {
			Lox.error(line, "Multiline comment not closed");
		} else {
			drop('*');
			drop('/');
		}
	}

	private void drop(char c) {
		char character = advance();
		if (character != c) {
			Lox.error(line, "Expected character '" + c + "'; got '" + character + "'.");
		}
	}

	private void consumeLineComment() {
		while (peek() != '\n' && !isAtEnd())
			advance();
	}

	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	private void number() {
		while (isDigit(peek()))
			advance();

		// Look for a fractional part.
		if (peek() == '.' && isDigit(peekNext())) {
			// Consume the "."
			advance();

			while (isDigit(peek()))
				advance();
		}

		addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
	}

	private void identifier() {
		while (isAlphaNumeric(peek()))
			advance();

		String text = source.substring(start, current);

		TokenType type = keywords.getOrDefault(text, IDENTIFIER);
		addToken(type);
	}

	private char peekNext() {
		if (current + 1 >= source.length())
			return '\0';
		return source.charAt(current + 1);
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private boolean match(char expected) {
		if (isAtEnd())
			return false;
		if (source.charAt(current) != expected)
			return false;

		current++;
		return true;
	}

	private char advance() {
		current++;
		return source.charAt(current - 1);
	}

	private void addToken(TokenType type) {
		addToken(type, null);
	}

	private char peek() {
		if (isAtEnd())
			return '\0';
		return source.charAt(current);
	}

	private void string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n')
				line++;
			advance();
		}

		// Unterminated string.
		if (isAtEnd()) {
			Lox.error(line, "Unterminated string.");
			return;
		}

		// The closing ".
		advance();

		// Trim the surrounding quotes.
		String value = source.substring(start + 1, current - 1);
		addToken(STRING, value);
	}

	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}

	static {
		keywords = new HashMap<>();
		keywords.put("and", AND);
		keywords.put("break", BREAK);
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("for", FOR);
		keywords.put("fun", FUN);
		keywords.put("if", IF);
		keywords.put("nil", NIL);
		keywords.put("or", OR);
		keywords.put("print", PRINT);
		keywords.put("return", RETURN);
		keywords.put("super", SUPER);
		keywords.put("this", THIS);
		keywords.put("true", TRUE);
		keywords.put("var", VAR);
		keywords.put("while", WHILE);
	}
}