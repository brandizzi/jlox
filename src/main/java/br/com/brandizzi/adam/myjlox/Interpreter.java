package br.com.brandizzi.adam.myjlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.brandizzi.adam.myjlox.Expr.Binary;
import br.com.brandizzi.adam.myjlox.Expr.Function;
import br.com.brandizzi.adam.myjlox.Expr.Grouping;
import br.com.brandizzi.adam.myjlox.Expr.Literal;
import br.com.brandizzi.adam.myjlox.Expr.Ternary;
import br.com.brandizzi.adam.myjlox.Expr.Unary;
import br.com.brandizzi.adam.myjlox.Expr.Variable;
import br.com.brandizzi.adam.myjlox.Stmt.Break;

public class Interpreter implements Stmt.Visitor<Void>, Expr.Visitor<Object> {
    Object uninitializedValue = new Object();

    private Environment environment = new Environment();
    final Environment globals = environment;
    String lastExpressionValue;

    private boolean breakLoop = false;

    private Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object
                call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }
        });
    }

    void interpret(List<Stmt> stmts) {
        try {
            for (Stmt statement : stmts) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        environment.define(stmt.name.lexeme, null);
        LoxClass klass = new LoxClass(stmt.name.lexeme);
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = uninitializedValue;
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
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof LoxInstance) {
            return ((LoxInstance) object).get(expr.name);
        }

        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof LoxInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((LoxInstance) object).set(expr.name, value);
        return value;
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
            throw new RuntimeError(
                expr.operator, "Operands must be two numbers or two strings."
            );
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
        Object value = lookUpVariable(expr);

        if (value == uninitializedValue) {
            throw new RuntimeError(
                expr.name,
                "Variable " + expr.name.lexeme + " used before initialization."
            );
        }

        return value;
    }

    private Object lookUpVariable(Expr.Variable expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, expr.index);
        } else {
            return globals.get(expr.name);
        }
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.index, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                if (breakLoop)
                    break;
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (!breakLoop && isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        breakLoop = false;
        return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Void visitBreakStmt(Break stmt) {
        breakLoop = true;
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(
                expr.paren, "Can only call functions and classes."
            );
        }

        LoxCallable function = (LoxCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(
                expr.paren,
                "Expected " + function.arity() + " arguments but got "
                    + arguments.size() + "."
            );
        }

        return function.call(this, arguments);
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null)
            value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Object visitFunctionExpr(Function expr) {
        return new LoxFunction(expr, environment);
    }

    public void resolve(Indexed expr, int depth, int index) {
        expr.index = index;
        locals.put(expr, depth);
    }
}
