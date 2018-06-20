package br.com.brandizzi.adam.myjlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Environment {
    final Environment enclosing;
    private final Map<String, Integer> indexMap = new HashMap<>();
    private final List<Object> values = new ArrayList<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    int define(String name, Object value) {
        values.add(value);
        int index = values.size() - 1;
        indexMap.put(name, index);
        return index;
    }

    Object get(Token name) {
        try {
            return get(name.lexeme);
        } catch (RuntimeError e) {
            throw new RuntimeError(name, e.getMessage());
        }
    }

    Object get(String name) {
        if (indexMap.containsKey(name)) {
            return get(indexMap.get(name));
        }

        if (enclosing != null)
            return enclosing.get(name);
        else
            throw new RuntimeError(null, "Undefined variable '" + name + "'.");
    }

    void assign(Token name, Object value) {
        String lexeme = name.lexeme;
        try {
            assign(lexeme, value);
        } catch (RuntimeError e) {
            throw new RuntimeError(name, e.getMessage());
        }
    }

    void assign(String name, Object value) {
        if (indexMap.containsKey(name)) {
            assign(indexMap.get(name), value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        } else {
            throw new RuntimeError(null, "Undefined variable '" + name + "'.");
        }
    }

    Object getAt(int distance, String name) {
        return ancestor(distance).get(name);
    }

    Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    void assignAt(int distance, Token name, Object value) {
        ancestor(distance).assign(name.lexeme, value);
    }

    Object get(int index) {
        return values.get(index);
    }

    Object getAt(int distance, int index) {
        return ancestor(distance).get(index);
    }

    void assign(int index, Object value) {
        values.set(index, value);
    }

    public void assignAt(int distance, int index, Object value) {
        ancestor(distance).assign(index, value);
    }
}