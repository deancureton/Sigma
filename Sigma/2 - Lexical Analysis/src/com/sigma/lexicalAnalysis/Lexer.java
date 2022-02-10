package com.sigma.lexicalAnalysis;

import static com.sigma.lexicalAnalysis.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private final String source;
    private final ArrayList<Lexeme> lexemes;
    private final HashMap<String, TokenType> keywords;

    private int currentPosition;
    private int startOfCurrentLexeme;
    private int lineNumber;

    public Lexer(String source) {
        this.source = source;
        this.lexemes = new ArrayList<>();
        this.keywords = getKeywords();

        this.currentPosition = 0;
        this.startOfCurrentLexeme = 0;
        this.lineNumber = 1;
    }

    private HashMap<String, TokenType> getKeywords() {
        HashMap<String, TokenType> keywords = new HashMap<>();
        keywords.put("num", NUM_KEYWORD);
        return keywords;
    }

    private void skipWhitespace() {

    }

    public ArrayList<Lexeme> lex() {
        return this.lexemes;
    }

    public void printLexemes() {
        System.out.println("Lexemes found: ");
        for (Lexeme lexeme : this.lexemes) {
            System.out.println(lexeme);
        }
    }
}
