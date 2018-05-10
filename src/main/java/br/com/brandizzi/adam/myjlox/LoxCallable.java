package br.com.brandizzi.adam.myjlox;

import java.util.List;

interface LoxCallable {
	Object call(Interpreter interpreter, List<Object> arguments);

	int arity();
}
