package br.com.brandizzi.adam.myjlox;

import java.util.Collections;
import java.util.Map;

public class LoxType extends LoxClass {

    LoxType(String name, Map<String, LoxFunction> methods) {
        super(name + " metaclass", methods);
    }
    
}
