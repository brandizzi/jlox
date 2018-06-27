package br.com.brandizzi.adam.myjlox;

import java.util.List;

class LoxFunction implements LoxCallable {
    protected final Environment closure;
    protected List<Token> parameters;
    protected List<Stmt> body;
    protected String name;
    protected final boolean isInitializer;

    LoxFunction(Stmt.Function declaration, Environment closure,
                boolean isInitializer) {
        this(declaration.parameters, declaration.body, closure, isInitializer);
        name = declaration.name.lexeme;
    }

    LoxFunction(List<Token> parameters, List<Stmt> body, Environment closure, boolean isInitializer) {
        this.isInitializer = isInitializer;
        this.parameters = parameters;
        this.body = body;
        this.closure = closure;
    }

    LoxFunction(Expr.Function expression, Environment closure) {
        this(expression.parameters, expression.body, closure, false);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);

        for (int i = 0; i < parameters.size(); i++) {
            environment.define(parameters.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(body, environment);
        } catch (Return r) {
            if (isInitializer)
                return closure.getAt(0, "this");
            return r.value;
        }
        if (isInitializer)
            return closure.getAt(0, "this");
        return null;
    }

    @Override
    public int arity() {
        return parameters.size();
    }

    @Override
    public String toString() {
        if (name != null) {
            return "<fn " + name + ">";
        } else {
            return "<anon fn>";
        }
    }

    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(parameters, body, environment, isInitializer);
    }

}