package br.com.brandizzi.adam.myjlox;

import static br.com.brandizzi.adam.myjlox.TokenType.AND;
import static br.com.brandizzi.adam.myjlox.TokenType.BANG;
import static br.com.brandizzi.adam.myjlox.TokenType.BANG_EQUAL;
import static br.com.brandizzi.adam.myjlox.TokenType.BREAK;
import static br.com.brandizzi.adam.myjlox.TokenType.COMMA;
import static br.com.brandizzi.adam.myjlox.TokenType.ELSE;
import static br.com.brandizzi.adam.myjlox.TokenType.EOF;
import static br.com.brandizzi.adam.myjlox.TokenType.EQUAL_EQUAL;
import static br.com.brandizzi.adam.myjlox.TokenType.FALSE;
import static br.com.brandizzi.adam.myjlox.TokenType.FOR;
import static br.com.brandizzi.adam.myjlox.TokenType.GREATER;
import static br.com.brandizzi.adam.myjlox.TokenType.GREATER_EQUAL;
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
import static br.com.brandizzi.adam.myjlox.TokenType.QUESTION;
import static br.com.brandizzi.adam.myjlox.TokenType.RIGHT_BRACE;
import static br.com.brandizzi.adam.myjlox.TokenType.RIGHT_PAREN;
import static br.com.brandizzi.adam.myjlox.TokenType.SEMICOLON;
import static br.com.brandizzi.adam.myjlox.TokenType.SLASH;
import static br.com.brandizzi.adam.myjlox.TokenType.STAR;
import static br.com.brandizzi.adam.myjlox.TokenType.STRING;
import static br.com.brandizzi.adam.myjlox.TokenType.TRUE;
import static br.com.brandizzi.adam.myjlox.TokenType.VAR;
import static br.com.brandizzi.adam.myjlox.TokenType.WHILE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Parser {
	private final List<Token> tokens;
	private int current = 0;
	private boolean inLoop = false;

	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	List<Stmt> parse() {
		List<Stmt> statements = new ArrayList<>();
		while (!isAtEnd()) {
			statements.add(declaration());
		}

		return statements;
	}

	private Stmt declaration() {
		try {
			if (match(TokenType.VAR))
				return varDeclaration();

			return statement();
		} catch (ParseError error) {
			synchronize();
			return null;
		}
	}

	private Stmt varDeclaration() {
		Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

		Expr initializer = null;
		if (match(TokenType.EQUAL)) {
			initializer = expression();
		}

		consume(SEMICOLON, "Expect ';' after variable declaration.");
		return new Stmt.Var(name, initializer);
	}

	private Stmt statement() {
		if (match(TokenType.PRINT))
			return printStatement();
		if (match(LEFT_BRACE))
			return new Stmt.Block(block());
		if (match(IF))
			return ifStatement();
		if (match(WHILE))
			return whileStatement();
		if (match(FOR))
			return forStatement();
		if (match(BREAK))
			return breakStatement();

		return expressionStatement();
	}

	private Stmt breakStatement() {
		if (!inLoop) {
			error(previous(), "'break' outside loop.");
		}
		Token token = consume(TokenType.SEMICOLON, "Expect ';' after 'break'.");

		return new Stmt.Break(token);
	}

	private Stmt forStatement() {
		consume(LEFT_PAREN, "Expect '(' after 'for'.");

		Stmt initializer;
		if (match(SEMICOLON)) {
			initializer = null;
		} else if (match(VAR)) {
			initializer = varDeclaration();
		} else {
			initializer = expressionStatement();
		}

		Expr condition = null;
		if (!check(SEMICOLON)) {
			condition = expression();
		}
		consume(SEMICOLON, "Expect ';' after loop condition.");

		Expr increment = null;
		if (!check(RIGHT_PAREN)) {
			increment = expression();
		}
		consume(RIGHT_PAREN, "Expect ')' after for clauses.");

		inLoop = true;
		Stmt body = statement();

		if (increment != null) {
			body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
		}
		if (condition == null)
			condition = new Expr.Literal(true);
		body = new Stmt.While(condition, body);

		if (initializer != null) {
			body = new Stmt.Block(Arrays.asList(initializer, body));
		}
		inLoop = false;

		return body;
	}

	private Stmt whileStatement() {
		inLoop = true;
		consume(LEFT_PAREN, "Expect '(' after 'while'.");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expect ')' after condition.");
		Stmt body = statement();
		inLoop = false;

		return new Stmt.While(condition, body);
	}

	private Stmt ifStatement() {
		consume(LEFT_PAREN, "Expect '(' after 'if'.");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expect ')' after if condition.");

		Stmt thenBranch = statement();
		Stmt elseBranch = null;
		if (match(ELSE)) {
			elseBranch = statement();
		}

		return new Stmt.If(condition, thenBranch, elseBranch);
	}

	private List<Stmt> block() {
		List<Stmt> statements = new ArrayList<>();

		while (!check(RIGHT_BRACE) && !isAtEnd()) {
			statements.add(declaration());
		}

		consume(RIGHT_BRACE, "Expect '}' after block.");
		return statements;
	}

	private Stmt printStatement() {
		Expr value = expression();
		consume(SEMICOLON, "Expect ';' after value.");
		return new Stmt.Print(value);
	}

	private Stmt expressionStatement() {
		Expr expr = expression();
		consume(SEMICOLON, "Expect ';' after expression.");
		return new Stmt.Expression(expr);
	}

	private Expr expression() {
		return assignment();
	}

	private Expr assignment() {
		Expr expr = or();

		if (match(TokenType.EQUAL)) {
			Token equals = previous();
			Expr value = assignment();

			if (expr instanceof Expr.Variable) {
				Token name = ((Expr.Variable) expr).name;
				return new Expr.Assign(name, value);
			}

			error(equals, "Invalid assignment target.");
		}

		return expr;
	}

	private Expr or() {
		Expr expr = and();

		while (match(OR)) {
			Token operator = previous();
			Expr right = and();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	private Expr and() {
		Expr expr = equality();

		while (match(AND)) {
			Token operator = previous();
			Expr right = equality();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
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
		if (match(PLUS)) {
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

		if (match(TokenType.IDENTIFIER)) {
			return new Expr.Variable(previous());
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