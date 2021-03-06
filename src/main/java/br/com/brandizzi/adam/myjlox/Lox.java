package br.com.brandizzi.adam.myjlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError;
    static boolean hadRuntimeError;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    public static boolean hasError() {
        return hadError;
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null)
                break;

            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty())
                continue;

            if (!trimmedLine.endsWith(";")) {
                line += ";";
            }

            Object value = run(line);
            System.out.println(value);
            hadError = false;
        }
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static Object run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError)
            return null;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError)
            return null;

        if (statements == null)
            return null;
        Stmt lastStatement = statements.get(statements.size() - 1);

        interpreter.interpret(statements);

        if (lastStatement instanceof Stmt.Expression) {
            return interpreter.lastExpressionValue;
        }
        return null;
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err
            .println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void runtimeError(RuntimeError error) {
        System.err
            .println("[line " + error.token.line + "] " + error.getMessage());
        hadRuntimeError = true;
    }
}
