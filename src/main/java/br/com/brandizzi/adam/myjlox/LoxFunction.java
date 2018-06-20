package br.com.brandizzi.adam.myjlox;

import java.util.List;

class LoxFunction implements LoxCallable {
    private final Environment closure;
    private List<Token> parameters;
    private List<Stmt> body;
    private String name;

    LoxFunction(Stmt.Function declaration, Environment closure) {
        this(declaration.parameters, declaration.body, closure);
        name = declaration.name.lexeme;
    }

    LoxFunction(
        List<Token> parameters, List<Stmt> body, Environment closure
    ) {
        this.parameters = parameters;
        this.body = body;
        this.closure = closure;
    }

    LoxFunction(Expr.Function expression, Environment closure) {
        this(expression.parameters, expression.body, closure);
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
            return r.value;
        }
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
        return new LoxFunction(parameters, body, environment);
      }

}