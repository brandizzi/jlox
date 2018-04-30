package br.com.brandizzi.adam.myjlox;

import java.util.List;

abstract class Expr {
	interface Visitor<R> {
		R visitTernaryExpr(Ternary expr);

		R visitAssignExpr(Assign expr);

		R visitBinaryExpr(Binary expr);

		R visitGroupingExpr(Grouping expr);

		R visitLiteralExpr(Literal expr);

		R visitUnaryExpr(Unary expr);

		R visitVariableExpr(Variable expr);
	}

	static class Ternary extends Expr {
		Ternary(Expr first, Expr middle, Expr last) {
			this.first = first;
			this.middle = middle;
			this.last = last;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visitTernaryExpr(this);
		}

		final Expr first;
		final Expr middle;
		final Expr last;
	}

	static class Assign extends Expr {
		Assign(Token name, Expr value) {
			this.name = name;
			this.value = value;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignExpr(this);
		}

		final Token name;
		final Expr value;
	}

	static class Binary extends Expr {
		Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpr(this);
		}

		final Expr left;
		final Token operator;
		final Expr right;
	}

	static class Grouping extends Expr {
		Grouping(Expr expression) {
			this.expression = expression;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpr(this);
		}

		final Expr expression;
	}

	static class Literal extends Expr {
		Literal(Object value) {
			this.value = value;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpr(this);
		}

		final Object value;
	}

	static class Unary extends Expr {
		Unary(Token operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpr(this);
		}

		final Token operator;
		final Expr right;
	}

	static class Variable extends Expr {
		Variable(Token name) {
			this.name = name;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableExpr(this);
		}

		final Token name;
	}

	abstract <R> R accept(Visitor<R> visitor);
}
