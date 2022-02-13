package com.sigma.lexicalAnalysis;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static com.sigma.lexicalAnalysis.TokenType.*;

public class LexerTest {
    public static void runFile(String path) throws IOException {
        String sourceCode = getSourceCodeFromFile(path);
        run(sourceCode);

        //if (hadSyntaxError) System.exit(65);
        //if (hadRuntimeError) System.exit(70);
    }

    private static String getSourceCodeFromFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes, Charset.defaultCharset());
    }

    public static void run(String sourceCode) {
        Lexer lexer = new Lexer(sourceCode);
        ArrayList<Lexeme> lexemes = lexer.lex();
        lexer.printLexemes();
    }

    private static boolean singlePathProvided(String[] args) {
        if (args.length == 1) return true;
        return false;
    }

    public static void main(String[] args) throws IOException {
        try {
            if (singlePathProvided(args)) runFile(args[0]);
            else {
                System.out.println("Usage: sigma [path to .sigma file]");
                System.exit(64);
            }
        } catch (IOException exception) {
            throw new IOException(exception.toString());
        }
    }
}