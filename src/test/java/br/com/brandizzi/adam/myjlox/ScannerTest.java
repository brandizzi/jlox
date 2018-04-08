package br.com.brandizzi.adam.myjlox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class ScannerTest {

	@Test
	public void testEOF() {
		Scanner scanner = new Scanner("");

		List<Token> tokens = scanner.scanTokens();

		assertEquals(1, tokens.size());

		Token token = tokens.get(0);

		assertEquals(TokenType.EOF, token.type);
	}

	@Test
	public void testDot() {
		Scanner scanner = new Scanner(".");

		List<Token> tokens = scanner.scanTokens();

		assertEquals(2, tokens.size());

		Token token = tokens.get(0);

		assertEquals(TokenType.DOT, token.type);
	}

	@Test
	public void testLineComment() {
		Scanner scanner = new Scanner("// this . is ( my comment )");

		List<Token> tokens = scanner.scanTokens();

		assertEquals(1, tokens.size());

		Token token = tokens.get(0);

		assertEquals(TokenType.EOF, token.type);
	}

	@Test
	public void testMultilineComment() {
		Scanner scanner = new Scanner("/* this . is ( my comment ) */");

		List<Token> tokens = scanner.scanTokens();

		assertEquals(1, tokens.size());

		Token token = tokens.get(0);

		assertEquals(TokenType.EOF, token.type);
		assertEquals(1, token.line);
	}

	@Test
	public void testMultilineCommentIncrementsLines() {
		Scanner scanner = new Scanner(". /* this . \n is ( my comment\n ) */ .");

		List<Token> tokens = scanner.scanTokens();

		assertEquals(3, tokens.size());

		Token token = tokens.get(0);

		assertEquals(TokenType.DOT, token.type);
		assertEquals(1, token.line);

		token = tokens.get(1);

		assertEquals(TokenType.DOT, token.type);
		assertEquals(3, token.line);
	}

	@Test
	public void testNotClosedMultilineCommentResultsInError() {
		Scanner scanner = new Scanner("/* this . is ( my comment )");

		scanner.scanTokens();

		assertTrue(Lox.hasError());
	}
}
