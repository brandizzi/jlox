package br.com.brandizzi.adam.myjlox;

import br.com.brandizzi.adam.myjlox.Expr.Assign;
import br.com.brandizzi.adam.myjlox.Expr.Binary;
import br.com.brandizzi.adam.myjlox.Expr.Grouping;
import br.com.brandizzi.adam.myjlox.Expr.Literal;
import br.com.brandizzi.adam.myjlox.Expr.Logical;
import br.com.brandizzi.adam.myjlox.Expr.Ternary;
import br.com.brandizzi.adam.myjlox.Expr.Unary;
import br.com.brandizzi.adam.myjlox.Expr.Variable;

public class ReversePolishPrinter implements Expr.Visitor<String> {

	@Override
	public String visitBinaryExpr(Binary expr) {
		return expr.left.accept(this) + " " + expr.right.accept(this) + " " + expr.operator.lexeme;
	}

	@Override
	public String visitGroupingExpr(Grouping expr) {
		return expr.expression.accept(this);
	}

	@Override
	public String visitLiteralExpr(Literal expr) {
		return expr.value.toString();
	}

	@Override
	public String visitUnaryExpr(Unary expr) {
		return expr.right.accept(this) + " " + expr.operator.lexeme;
	}

	public String print(Expr expr) {
		return expr.accept(this);
	}

	public static void main(String[] args) {
		Expr expression = new Expr.Binary(new Expr.Literal(123), new Token(TokenType.STAR, "*", null, 1),
				new Expr.Grouping(new Expr.Binary(new Expr.Literal(45.67), new Token(TokenType.PLUS, "+", null, 1),
						new Expr.Literal(0.33))));

		System.out.println(new ReversePolishPrinter().print(expression));
	}

	@Override
	public String visitTernaryExpr(Ternary ternary) {
		throw new ParseError();
	}

	@Override
	public String visitAssignExpr(Assign expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitVariableExpr(Variable expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitLogicalExpr(Logical expr) {
		// TODO Auto-generated method stub
		return null;
	}

}
