package br.com.brandizzi.adam.myjlox;

import java.util.List;

import static br.com.brandizzi.adam.myjlox.TokenType.*;

class Parser {
	private final List<Token> tokens;
	private int current = 0;

	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	Expr parse() {
		try {
			return expression();
		} catch (ParseError error) {
			return null;
		}
	}

	private Expr expression() {
		return expressionSeries();
	}

	private Expr ternary() {
		if (match(QUESTION)) {
			error(previous(), "Question operator ('?') expects an expression at the left.");
			return equality();
		}
		Expr expr = equality();

		if (match(QUESTION)) {
			Expr middle = expression();
			consume(TokenType.COLON, "Expect ':' for '?' operator.");
			Expr last = ternary();
			expr = new Expr.Ternary(expr, middle, last);
		}
		return expr;
	}

	private Expr expressionSeries() {
		if (match(COMMA)) {
			error(previous(), "Comma operator (',') expects an expression at the left.");
			return ternary();
		}

		Expr expr = ternary();
		while (match(COMMA)) {
			Token operator = previous();
			Expr right = ternary();
			expr = new Expr.Binary(expr, operator, right);
		}
		return expr;
	}

	private Expr equality() {
		if (match(BANG_EQUAL, EQUAL_EQUAL)) {
			error(previous(), "Equality/inequality comparison expects an expression at the left.");
			return comparison();

		}
		Expr expr = comparison();

		while (match(BANG_EQUAL, EQUAL_EQUAL)) {
			Token operator = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr comparison() {
		if (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
			error(previous(), "Ordered comparisons expect an expression at the left.");
			return addition();

		}
		Expr expr = addition();

		while (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
			Token operator = previous();
			Expr right = addition();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr addition() {
		if (match( PLUS)) {
			error(previous(), "Addition and expects an expression at the left.\n");
			return multiplication();
		}
		Expr expr = multiplication();

		while (match(MINUS, PLUS)) {
			Token operator = previous();
			Expr right = multiplication();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr multiplication() {
		if (match(STAR, SLASH)) {
			error(previous(), "Multiplication and division expect an expression at the left.");
		}
		Expr expr = unary();

		while (match(STAR, SLASH)) {
			Token operator = previous();
			Expr right = unary();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr unary() {
		if (match(BANG, MINUS)) {
			Token operator = previous();
			Expr right = unary();
			return new Expr.Unary(operator, right);
		}

		return primary();
	}

	private Expr primary() {
		if (match(FALSE))
			return new Expr.Literal(false);
		if (match(TRUE))
			return new Expr.Literal(true);
		if (match(NIL))
			return new Expr.Literal(null);

		if (match(NUMBER, STRING)) {
			return new Expr.Literal(previous().literal);
		}

		if (match(LEFT_PAREN)) {
			Expr expr = expression();
			consume(RIGHT_PAREN, "Expect ')' after expression.");
			return new Expr.Grouping(expr);
		}

		throw error(peek(), "Expected expression.");

	}

	private void synchronize() {
		advance();

		while (!isAtEnd()) {
			if (previous().type == SEMICOLON)
				return;

			switch (peek().type) {
			case CLASS:
			case FUN:
			case VAR:
			case FOR:
			case IF:
			case WHILE:
			case PRINT:
			case RETURN:
				return;
			}

			advance();
		}
	}

	private Token consume(TokenType type, String message) {
		if (check(type))
			return advance();

		throw error(peek(), message);
	}

	private ParseError error(Token token, String message) {
		Lox.error(token, message);
		return new ParseError();
	}

	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}

		return false;
	}

	private boolean check(TokenType tokenType) {
		if (isAtEnd())
			return false;
		return peek().type == tokenType;
	}

	private Token advance() {
		if (!isAtEnd())
			current++;
		return previous();
	}

	private boolean isAtEnd() {
		return peek().type == EOF;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private Token previous() {
		return tokens.get(current - 1);
	}

	public static class ParserError extends Exception {
	}

}