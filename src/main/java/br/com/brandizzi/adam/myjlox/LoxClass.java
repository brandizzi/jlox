package br.com.brandizzi.adam.myjlox;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class LoxClass extends LoxInstance implements LoxCallable {
    final String name;
    private final Map<String, LoxFunction> methods;
    private final Map<String, LoxGetter> getters;
    private List<LoxClass> superclasses;

    LoxClass(
        String name, List<LoxClass> superclasses,
        Map<String, LoxFunction> methods, Map<String, LoxFunction> classMethods,
        Map<String, LoxGetter> getters
    ) {
        super(new LoxType(name, classMethods));
        this.superclasses = superclasses;
        this.name = name;
        this.methods = methods;
        this.getters = getters;

    }

    protected LoxClass(String name, Map<String, LoxFunction> classMethods) {
        super(null);
        this.name = name;
        this.methods = classMethods;
        this.getters = Collections.emptyMap();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = methods.get("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    LoxFunction findMethod(LoxInstance instance, String name) {
        if (methods.containsKey(name)) {
            return methods.get(name).bind(instance);
        }

        for (LoxClass superclass : superclasses) {
            LoxFunction method = superclass.findMethod(instance, name);
            if (method != null)
                return method;
        }

        return null;
    }

    LoxGetter findGetter(LoxInstance instance, String name) {
        if (getters.containsKey(name)) {
            return getters.get(name).bind(instance);
        }

        return null;
    }

    @Override
    public int arity() {
        LoxFunction initializer = methods.get("init");
        if (initializer == null)
            return 0;
        return initializer.arity();
    }
}