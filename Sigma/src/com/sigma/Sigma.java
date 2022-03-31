package com.sigma;

import com.sigma.environments.Environment;
import com.sigma.lexicalAnalysis.*;
import com.sigma.parsing.Parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Sigma {
    private static final ArrayList<String> syntaxErrorMessages = new ArrayList<>();
    private static final ArrayList<String> runtimeErrorMessages = new ArrayList<>();
    private static final ArrayList<String> referenceErrorMessages = new ArrayList<>();

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
        Parser parser = new Parser(lexer.lex());
        lexer.printLexemes();
        Lexeme programParseTree = parser.program();
        programParseTree.printTree();
    }

    private static String getSourceCodeFromFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes, Charset.defaultCharset());
    }

    public static void syntaxError(String message, int lineNumber) {
        syntaxErrorMessages.add("Syntax error (line " + lineNumber + "): " + message);
    }

    public static void syntaxError(String message, Lexeme lexeme) {
        syntaxErrorMessages.add("Syntax error at " + lexeme + ": " + message);
    }

    public static void runtimeError(String message, int lineNumber) {
        runtimeErrorMessages.add("Runtime error at line " + lineNumber + ": " + message);
        printErrors();
        System.exit(65);
    }

    public static void runtimeError(String message, Lexeme lexeme) {
        runtimeErrorMessages.add("Runtime error at " + lexeme + ": " + message);
        printErrors();
        System.exit(65);
    }

    public static void referenceError(String message, Lexeme lexeme) {
        referenceErrorMessages.add("Reference error at " + lexeme + ": " + message);
        printErrors();
        System.exit(66);
    }

    private static void printErrors() {
        final String ANSI_YELLOW = "\u001B[33m";
        final String ANSI_RED_BACKGROUND = "\u001B[41m";
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_RESET = "\u001B[0m";
        for (String syntaxErrorMessage : syntaxErrorMessages) {
            System.out.println(ANSI_YELLOW + syntaxErrorMessage + ANSI_RESET);
        }
        for (String runtimeErrorMessage : runtimeErrorMessages) {
            System.out.println(ANSI_RED_BACKGROUND + runtimeErrorMessage + ANSI_RESET);
        }
        for (String referenceErrorMessage : referenceErrorMessages) {
            System.out.println(ANSI_RED + referenceErrorMessage + ANSI_RESET);
        }
    }
}
