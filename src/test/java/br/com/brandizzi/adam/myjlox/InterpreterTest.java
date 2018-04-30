package br.com.brandizzi.adam.myjlox;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class InterpreterTest {

	@Test
	public void testInterpet() {
		List<Stmt> stmts = getStatements("var a;");
		
		
		Interpreter interpreter = new Interpreter();
		interpreter.interpret(stmts);
	}

	@Test
	public void testInterpetRuntimeError() {
		List<Stmt> stmts = getStatements("2-\"muffin\";");
		
		Interpreter interpreter = new Interpreter();
		interpreter.interpret(stmts);
		assertTrue(Lox.hadRuntimeError);
	}
	
	@Test
	public void testInterpetUseUninitializedGiveError() {
		List<Stmt> stmts = getStatements("var a; print a;");
		
		Interpreter interpreter = new Interpreter();
		interpreter.interpret(stmts);
		assertTrue(Lox.hadRuntimeError);
	}
	
	private List<Stmt> getStatements(String src) {
		Scanner scanner = new Scanner(src);
		List<Token> tokens = scanner.scanTokens();
		Parser parser = new Parser(tokens);
		List<Stmt> stmts = parser.parse();
		return stmts;
	}
}
	
