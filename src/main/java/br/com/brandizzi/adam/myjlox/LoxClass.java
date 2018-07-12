package br.com.brandizzi.adam.myjlox;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class LoxClass extends LoxInstance implements LoxCallable {
    final String name;
    private final Map<String, LoxFunction> methods;
    private final Map<String, LoxGetter> getters;
    private LoxClass superclass;

    LoxClass(
        String name, LoxClass superclass, Map<String, LoxFunction> methods,
        Map<String, LoxFunction> classMethods, Map<String, LoxGetter> getters
    ) {
        super(new LoxType(name, classMethods));
        this.superclass = superclass;
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

        if (superclass != null) {
            return superclass.findMethod(instance, name);
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