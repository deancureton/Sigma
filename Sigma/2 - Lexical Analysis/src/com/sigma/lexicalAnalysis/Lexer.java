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
        keywords.put("‼️", BANGBANG);
        keywords.put("{", OPEN_CURLY);
        keywords.put("}", CLOSED_CURLY);
        keywords.put("|", VERTICAL_PIPE);
        keywords.put("[", OPEN_SQUARE);
        keywords.put("]", CLOSED_SQUARE);
        keywords.put("+", PLUS);
        keywords.put("-", MINUS);
        keywords.put("*", TIMES);
        keywords.put("/", DIVIDE);
        keywords.put("^", CARET);
        keywords.put("%", PERCENT);
        keywords.put("?", QUESTION);
        keywords.put("~", APPROX);
        keywords.put(">", GREATER);
        keywords.put("<", LESS);
        keywords.put("≥", GEQ);
        keywords.put("≤", LEQ);
        keywords.put(".", PERIOD);
        keywords.put("\\", BACKSLASH);
        keywords.put("<-", ASSIGNMENT);
        keywords.put("!?", NOT_QUESTION);
        keywords.put("!~", NOT_APPROX);
        keywords.put("??", DOUBLE_QUESTION);
        keywords.put(">?", GREATER_QUESTION);
        keywords.put("<?", LESS_QUESTION);
        keywords.put("//", DOUBLE_DIVIDE);
        keywords.put("++", INCREMENT);
        keywords.put("--", DECREMENT);
        keywords.put("\\.", BACKSLASH_PERIOD);
        keywords.put(".\\", PERIOD_BACKSLASH);
        keywords.put("!??", NOT_DOUBLE_QUESTION);
        keywords.put("+<-", PLUS_ASSIGNMENT);
        keywords.put("-<-", MINUS_ASSIGNMENT);
        keywords.put("*<-", TIMES_ASSIGNMENT);
        keywords.put("/<-", DIVIDE_ASSIGNMENT);
        keywords.put("^<-", CARET_ASSIGNMENT);
        keywords.put("%<-", PERCENT_ASSIGNMENT);
        keywords.put("num", NUM_KEYWORD);
        keywords.put("str", STR_KEYWORD);
        keywords.put("tf", TF_KEYWORD);
        keywords.put("var", VAR_KEYWORD);
        keywords.put("func", FUNC_KEYWORD);
        keywords.put("arr", ARR_KEYWORD);
        keywords.put("for", FOR_KEYWORD);
        keywords.put("when", WHEN_KEYWORD);
        keywords.put("loop", LOOP_KEYWORD);
        keywords.put("of", OF_KEYWORD);
        keywords.put("count", COUNT_KEYWORD);
        keywords.put("nothing", NOTHING_KEYWORD);
        keywords.put("and", AND_KEYWORD);
        keywords.put("or", OR_KEYWORD);
        keywords.put("not", NOT_KEYWORD);
        keywords.put("nand", NAND_KEYWORD);
        keywords.put("nor", NOR_KEYWORD);
        keywords.put("xor", XOR_KEYWORD);
        keywords.put("xnor", XNOR_KEYWORD);
        keywords.put("implies", IMPLIES_KEYWORD);
        keywords.put("if", IF_KEYWORD);
        keywords.put("butif", BUTIF_KEYWORD);
        keywords.put("but", BUT_KEYWORD);
        keywords.put("true", TRUE_KEYWORD);
        keywords.put("fals", FALS_KEYWORD);
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
