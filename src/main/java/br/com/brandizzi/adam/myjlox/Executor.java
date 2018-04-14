package br.com.brandizzi.adam.myjlox;

class Executor implements Expr.Visitor<Double> {
	double calculate(Expr expr) {
		return expr.accept(this);
	}

	@Override
	public Double visitBinaryExpr(Expr.Binary expr) {
		switch (expr.operator.type) {
		case PLUS:
			return expr.left.accept(this) + expr.right.accept(this);
		case MINUS:
			return expr.left.accept(this) - expr.right.accept(this);
		case STAR:
			return expr.left.accept(this) * expr.right.accept(this);
		case SLASH:
			return expr.left.accept(this) / expr.right.accept(this);
		default:
			return Double.NaN;
		}
	}

	@Override
	public Double visitGroupingExpr(Expr.Grouping expr) {
		return expr.expression.accept(this);
	}

	@Override
	public Double visitLiteralExpr(Expr.Literal expr) {
		if (expr.value instanceof Number)
			return Double.valueOf(expr.value.toString());
		Lox.error(0, expr.value.toString());
		throw new ParseError();
	}

	@Override
	public Double visitUnaryExpr(Expr.Unary expr) {
		if (expr.operator.type == TokenType.MINUS) {
			return -Double.valueOf(expr.right.accept(this).toString());
		}
		throw new ParseError();
	}
	
	public static void main(String[] args) {
		Expr expression = new Expr.Binary(
				new Expr.Literal(2),
				new Token(TokenType.STAR, "*", null, 1),
				new Expr.Grouping(
					new Expr.Binary(new Expr.Literal(2.5), new Token(TokenType.PLUS, "+", null, 3), new Expr.Literal(2))
				)
			);

		System.out.println(new Executor().calculate(expression));
	}
}