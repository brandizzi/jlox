package br.com.brandizzi.adam.myjlox;

import br.com.brandizzi.adam.myjlox.Expr.Assign;
import br.com.brandizzi.adam.myjlox.Expr.Logical;
import br.com.brandizzi.adam.myjlox.Expr.Ternary;
import br.com.brandizzi.adam.myjlox.Expr.Variable;

class AstPrinter implements Expr.Visitor<String> {
	String print(Expr expr) {
		return expr.accept(this);
	}

	@Override
	public String visitBinaryExpr(Expr.Binary expr) {
		return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitGroupingExpr(Expr.Grouping expr) {
		return parenthesize("group", expr.expression);
	}

	@Override
	public String visitLiteralExpr(Expr.Literal expr) {
		if (expr.value == null)
			return "nil";
		return expr.value.toString();
	}

	@Override
	public String visitUnaryExpr(Expr.Unary expr) {
		return parenthesize(expr.operator.lexeme, expr.right);
	}

	private String parenthesize(String name, Expr... exprs) {
		StringBuilder builder = new StringBuilder();

		builder.append("(").append(name);
		for (Expr expr : exprs) {
			builder.append(" ");
			builder.append(expr.accept(this));
		}
		builder.append(")");

		return builder.toString();
	}

	@Override
	public String visitTernaryExpr(Ternary ternary) {
		return parenthesize("ternary", ternary.first, ternary.middle, ternary.last);
	}

	@Override
	public String visitAssignExpr(Assign expr) {
		return "(\u21e6" + expr.name.lexeme + expr.accept(this);
	}

	@Override
	public String visitVariableExpr(Variable expr) {
		return "(var" + expr.name + ")";
	}

	@Override
	public String visitLogicalExpr(Logical expr) {
		// TODO Auto-generated method stub
		return null;
	}

}
