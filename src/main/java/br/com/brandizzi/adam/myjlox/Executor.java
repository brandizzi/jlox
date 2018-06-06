package br.com.brandizzi.adam.myjlox;

import br.com.brandizzi.adam.myjlox.Expr.Assign;
import br.com.brandizzi.adam.myjlox.Expr.Call;
import br.com.brandizzi.adam.myjlox.Expr.Function;
import br.com.brandizzi.adam.myjlox.Expr.Logical;
import br.com.brandizzi.adam.myjlox.Expr.Ternary;
import br.com.brandizzi.adam.myjlox.Expr.Variable;

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
		// Expr expression = new Expr.Ternary(
		// new Expr.Literal(1),
		// new Expr.Binary(
		// new Expr.Literal(2),
		// new Token(TokenType.STAR, "*", null, 1),
		// new Expr.Grouping(
		// new Expr.Binary(new Expr.Literal(2.5), new Token(TokenType.PLUS, "+", null,
		// 3), new Expr.Literal(2))
		// )
		// ),
		// new Expr.Literal(1)
		// );

		Expr expression = new Expr.Ternary(new Expr.Literal(0),
				new Expr.Ternary(new Expr.Literal(0), new Expr.Literal(2), new Expr.Literal(1)),
				new Expr.Ternary(new Expr.Literal(0), new Expr.Literal(3), new Expr.Literal(4)));

		System.out.println(new Executor().calculate(expression));
	}

	@Override
	public Double visitTernaryExpr(Ternary ternary) {
		Double condition = ternary.first.accept(this);
		if (Math.abs(condition) > 0.0001) {
			return ternary.middle.accept(this);
		}
		return ternary.last.accept(this);
	}

	@Override
	public Double visitAssignExpr(Assign expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visitVariableExpr(Variable expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visitLogicalExpr(Logical expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double visitCallExpr(Call expr) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public Double visitFunctionExpr(Function expr) {
        // TODO Auto-generated method stub
        return null;
    }
}