package br.com.brandizzi.adam.myjlox;

import java.util.Collections;
import java.util.List;

class LoxGetter extends LoxFunction {

    LoxGetter(Stmt.Function declaration, Environment closure) {
        super(declaration, closure, false);
    }

    LoxGetter(List<Stmt> body, Environment closure) {
        super(Collections.<Token>emptyList(), body, closure, false);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);

        try {
            interpreter.executeBlock(body, environment);
        } catch (Return r) {
            return r.value;
        }
        return null;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public String toString() {
        return "<getter " + name + ">";
    }


    LoxGetter bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxGetter(body, environment);
    }
}