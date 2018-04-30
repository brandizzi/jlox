package br.com.brandizzi.adam.myjlox;

import java.util.List;

import br.com.brandizzi.adam.myjlox.Expr.Binary;
import br.com.brandizzi.adam.myjlox.Expr.Grouping;
import br.com.brandizzi.adam.myjlox.Expr.Literal;
import br.com.brandizzi.adam.myjlox.Expr.Ternary;
import br.com.brandizzi.adam.myjlox.Expr.Unary;
import br.com.brandizzi.adam.myjlox.Expr.Variable;

public class Interpreter implements Stmt.Visitor<Void>, Expr.Visitor<Object> {

	private Environment environment = new Environment();
	String lastExpressionValue;

	void interpret(List<Stmt> stmts) {
		try {
			for (Stmt statement : stmts) {
				execute(statement);
			}
		} catch (RuntimeError error) {
			Lox.runtimeError(error);
		}
	}

	String interpret(Expr expression) {
		return stringify(evaluate(expression));
	}

	private void execute(Stmt stmt) {
		stmt.accept(this);
	}

	@Override
	public Void visitVarStmt(Stmt.Var stmt) {
		Object value = null;
		if (stmt.initializer != null) {
			value = evaluate(stmt.initializer);
		}

		environment.define(stmt.name.lexeme, value);
		return null;
	}

	@Override
	public Void visitPrintStmt(Stmt.Print stmt) {
		Object value = evaluate(stmt.expression);
		System.out.println(stringify(value));
		return null;
	}

	@Override
	public Void visitExpressionStmt(Stmt.Expression stmt) {
		lastExpressionValue = stringify(evaluate(stmt.expression));
		return null;
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

			if (left instanceof String || right instanceof String) {
				return stringify(left) + stringify(right);
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
			checkDivisionByZero(expr.operator, (double) right);
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

	private void checkDivisionByZero(Token operator, double right) {
		if (right == 0) {
			throw new RuntimeError(operator, "Divisor cannot be zero.");
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
	public Object visitTernaryExpr(Ternary ternary) {
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

	@Override
	public Object visitVariableExpr(Variable expr) {
		return environment.get(expr.name);
	}

	@Override
	public Object visitAssignExpr(Expr.Assign expr) {
		Object value = evaluate(expr.value);

		environment.assign(expr.name, value);
		return value;
	}

	@Override
	public Void visitBlockStmt(Stmt.Block stmt) {
		executeBlock(stmt.statements, new Environment(environment));
		return null;
	}

	void executeBlock(List<Stmt> statements, Environment environment) {
		Environment previous = this.environment;
		try {
			this.environment = environment;

			for (Stmt statement : statements) {
				execute(statement);
			}
		} finally {
			this.environment = previous;
		}
	}

}
