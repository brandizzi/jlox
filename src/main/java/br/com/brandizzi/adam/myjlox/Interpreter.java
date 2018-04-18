package br.com.brandizzi.adam.myjlox;

import br.com.brandizzi.adam.myjlox.Expr.Binary;
import br.com.brandizzi.adam.myjlox.Expr.Grouping;
import br.com.brandizzi.adam.myjlox.Expr.Literal;
import br.com.brandizzi.adam.myjlox.Expr.Ternary;
import br.com.brandizzi.adam.myjlox.Expr.Unary;
import br.com.brandizzi.adam.myjlox.Expr.Visitor;

public class Interpreter implements Visitor<Object> {

	void interpret(Expr expression) {
		try {
			Object value = evaluate(expression);
			System.out.println(stringify(value));
		} catch (RuntimeError error) {
			Lox.runtimeError(error);
		}
	}

	private String stringify(Object object) {
		if (object == null)
			return "nil";

		// Hack. Work around Java adding ".0" to integer-valued doubles.
		if (object instanceof Double) {
			String text = object.toString();
			if (text.endsWith(".0")) {
				text = text.substring(0, text.length() - 2);
			}
			return text;
		}

		return object.toString();
	}

	@Override
	public Object visitLiteralExpr(Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitGroupingExpr(Grouping expr) {
		return evaluate(expr.expression);
	}

	private Object evaluate(Expr expression) {
		return expression.accept(this);
	}

	@Override
	public Object visitUnaryExpr(Unary expr) {
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
		case MINUS:
			checkNumberOperand(expr.operator, right);
			return -(double) right;
		case BANG:
			return !isTruthy(right);
		default:
			return null;
		}
	}

	@Override
	public Object visitBinaryExpr(Binary expr) {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
		case PLUS:
			if (left instanceof Double && right instanceof Double) {
				return (double) left + (double) right;
			}

			if (left instanceof String && right instanceof String) {
				return (String) left + (String) right;
			}
			throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
		case MINUS:
			checkNumberOperands(expr.operator, left, right);
			return (double) left - (double) right;
		case STAR:
			checkNumberOperands(expr.operator, left, right);
			return (double) left * (double) right;
		case SLASH:
			checkNumberOperands(expr.operator, left, right);
			return (double) left / (double) right;
		case GREATER:
			checkNumberOperands(expr.operator, left, right);
			return (double) left > (double) right;
		case GREATER_EQUAL:
			checkNumberOperands(expr.operator, left, right);
			return (double) left >= (double) right;
		case LESS:
			checkNumberOperands(expr.operator, left, right);
			return (double) left < (double) right;
		case LESS_EQUAL:
			checkNumberOperands(expr.operator, left, right);
			return (double) left <= (double) right;
		case BANG_EQUAL:
			return !isEqual(left, right);
		case EQUAL_EQUAL:
			return isEqual(left, right);
		case COMMA:
			return right;
		default:
			return null;
		}
	}

	private void checkNumberOperands(Token token, Object left, Object right) {
		if (left instanceof Double && right instanceof Double)
			return;

		throw new RuntimeError(token, "Operands must be numbers.");
	}

	private boolean isEqual(Object a, Object b) {
		// nil is only equal to nil.
		if (a == null && b == null)
			return true;
		if (a == null)
			return false;

		return a.equals(b);
	}

	private boolean isTruthy(Object object) {
		if (object == null)
			return false;
		if (object instanceof Boolean)
			return (boolean) object;
		return true;
	}

	@Override
	public Object visitTernary(Ternary ternary) {
		Object first = evaluate(ternary.first);

		if (isTruthy(first)) {
			return evaluate(ternary.middle);
		} else {
			return evaluate(ternary.last);
		}
	}

	private void checkNumberOperand(Token operator, Object operand) {
		if (operand instanceof Double)
			return;
		throw new RuntimeError(operator, "Operand must be a number.");
	}
}
