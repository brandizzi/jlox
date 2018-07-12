package br.com.brandizzi.adam.myjlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import br.com.brandizzi.adam.myjlox.Expr.Function;
import br.com.brandizzi.adam.myjlox.Expr.Ternary;
import br.com.brandizzi.adam.myjlox.Stmt.Break;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private final Stack<Integer> counts = new Stack<>();
    private final Stack<Map<String, Integer>> indicesMaps = new Stack<>();
    private final Stack<List<Token>> useCheck = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    private enum ClassType {
        NONE, CLASS
    }

    private ClassType currentClass = ClassType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
        indicesMaps.push(new HashMap<String, Integer>());
        counts.push(-1);
        useCheck.push(new ArrayList<Token>());

    }

    private void endScope() {
        List<Token> count = useCheck.pop();
        for (Token token : count) {
            if (token.type != TokenType.THIS && token.type != TokenType.SUPER)
                Lox.error(token, "Local variable never used.");
        }
        indicesMaps.pop();
        counts.pop();
        scopes.pop();
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        declare(stmt.name);

        if (stmt.superclass != null) {
            resolve(stmt.superclass);
        }

        define(stmt.name);

        if (stmt.superclass != null) {
            beginScope();
            Token sup = new Token(TokenType.SUPER, "super", "super", 0);

            declare(sup);
        }
        
        for (Stmt.Function method : stmt.classMethods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.name.lexeme.equals("init")) {
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
        }

        beginScope();

        Token that = new Token(TokenType.THIS, "this", "this", 0);
        declare(that);

        for (Stmt.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.name.lexeme.equals("init")) {
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
        }

        for (Stmt.Function getter : stmt.getters) {
            FunctionType declaration = FunctionType.METHOD;
            resolveFunction(getter, declaration);
        }

        endScope();

        if (stmt.superclass != null)
            endScope();

        currentClass = enclosingClass;

        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
      resolveLocal(expr, expr.keyword);
      return null;
    }
    
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void declare(Token name) {
        if (scopes.isEmpty())
            return;

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Lox.error(
                name, "Variable with this name already declared in this scope."
            );
        }

        scope.put(name.lexeme, false);

        int count = counts.pop();
        count += 1;
        counts.push(count);
        Map<String, Integer> indexMap = indicesMaps.peek();
        indexMap.put(name.lexeme, count);

        List<Token> check = useCheck.peek();
        check.add(name);
    }

    private void define(Token name) {
        if (scopes.isEmpty())
            return;
        scopes.peek().put(name.lexeme, true);
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty()
            && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Lox.error(
                expr.name, "Cannot read local variable in its own initializer."
            );
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    private void resolveLocal(Var expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                Map<String, Integer> indexMap = indicesMaps.get(i);
                interpreter.resolve(
                    expr, scopes.size() - 1 - i, indexMap.get(name.lexeme)
                );

                List<Token> it = useCheck.get(i);
                List<Token> count = new ArrayList<Token>(it);
                for (Token token : count) {
                    if (token.lexeme.equals(name.lexeme)) {
                        it.remove(token);
                    }
                }
                return;
            }
        }

        // Not found. Assume it is global.
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword, "Cannot use 'this' outside of a class.");
            return null;
        }

        resolveLocal(expr, expr.keyword);
        return null;
    }

    private void resolveFunction(Stmt.Function function) {
        resolveFunction(function, FunctionType.FUNCTION);

    }

    private void
        resolveFunction(Stmt.Function function, FunctionType functionType) {
        resolveFunction(function.parameters, function.body, functionType);
    }

    private void resolveFunction(
        List<Token> parameters, List<Stmt> body, FunctionType type
    ) {

        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();

        for (Token param : parameters) {
            declare(param);
            define(param);
        }
        resolve(body);
        endScope();

        currentFunction = enclosingFunction;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null)
            resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Cannot return from top-level code.");
        }
        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                Lox.error(
                    stmt.keyword, "Cannot return a value from an initializer."
                );
            }
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitBreakStmt(Break stmt) {
        return null;
    }

    @Override
    public Void visitTernaryExpr(Ternary expr) {
        resolve(expr.first);
        resolve(expr.middle);
        resolve(expr.last);
        return null;
    }

    @Override
    public Void visitFunctionExpr(Function expr) {
        resolveFunction(expr.parameters, expr.body, FunctionType.FUNCTION);
        return null;
    }
}
