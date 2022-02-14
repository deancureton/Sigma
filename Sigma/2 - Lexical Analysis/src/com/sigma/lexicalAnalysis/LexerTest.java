package com.sigma.lexicalAnalysis;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static com.sigma.lexicalAnalysis.TokenType.*;

public class LexerTest {
    public static void main(String[] args) throws IOException {
        try {
            if (args.length == 1) runFile(args[0]);
            else {
                System.out.println("Usage: sigma [path to .sigma file]");
                System.exit(64);
            }
        } catch (IOException e) {
            throw new IOException(e.toString());
        }
    }

    public static void runFile(String path) throws IOException {
        if (!path.endsWith(".sigma")) path += ".sigma";
        System.out.println("Running " + path + "...");
        String sourceCode = getSourceCodeFromFile(path);
        Lexer lexer = new Lexer(sourceCode);
        ArrayList<Lexeme> lexemes = lexer.lex();
        lexer.printLexemes();
    }

    private static String getSourceCodeFromFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes, Charset.defaultCharset());
    }
}