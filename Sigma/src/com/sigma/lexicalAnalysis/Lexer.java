package com.sigma.lexicalAnalysis;

import com.sigma.Sigma;

import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    // Instance variables
    private final String source;
    private final ArrayList<Lexeme> lexemes = new ArrayList<>();
    private final HashMap<String, TokenType> keywords = getKeywords();

    private int currentPosition = 0;
    private int startOfCurrentLexeme = 0;
    private int lineNumber = 1;

    // Constructor
    public Lexer(String source) {
        this.source = source;
    }

    // Keywords
    private HashMap<String, TokenType> getKeywords() {
        HashMap<String, TokenType> keywords = new HashMap<>();
        keywords.put("num", TokenType.NUM_KEYWORD);
        keywords.put("str", TokenType.STR_KEYWORD);
        keywords.put("tf", TokenType.TF_KEYWORD);
        keywords.put("var", TokenType.VAR_KEYWORD);
        keywords.put("func", TokenType.FUNC_KEYWORD);
        keywords.put("arr", TokenType.ARR_KEYWORD);
        keywords.put("for", TokenType.FOR_KEYWORD);
        keywords.put("when", TokenType.WHEN_KEYWORD);
        keywords.put("loop", TokenType.LOOP_KEYWORD);
        keywords.put("change", TokenType.CHANGE_KEYWORD);
        keywords.put("of", TokenType.OF_KEYWORD);
        keywords.put("count", TokenType.COUNT_KEYWORD);
        keywords.put("nothing", TokenType.NOTHING_KEYWORD);
        keywords.put("and", TokenType.AND_KEYWORD);
        keywords.put("or", TokenType.OR_KEYWORD);
        keywords.put("not", TokenType.NOT_KEYWORD);
        keywords.put("nand", TokenType.NAND_KEYWORD);
        keywords.put("nor", TokenType.NOR_KEYWORD);
        keywords.put("xor", TokenType.XOR_KEYWORD);
        keywords.put("xnor", TokenType.XNOR_KEYWORD);
        keywords.put("implies", TokenType.IMPLIES_KEYWORD);
        keywords.put("if", TokenType.IF_KEYWORD);
        keywords.put("butif", TokenType.BUTIF_KEYWORD);
        keywords.put("but", TokenType.BUT_KEYWORD);
        keywords.put("true", TokenType.TRUE_KEYWORD);
        keywords.put("fals", TokenType.FALS_KEYWORD);
        return keywords;
    }

    // Helpers
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(currentPosition);
    }

    private char peekNext() {
        if (currentPosition + 1 >= source.length()) return '\0';
        return source.charAt(currentPosition + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(currentPosition) != expected) return false;
        currentPosition++;
        return true;
    }

    private char advance() {
        char currentChar = source.charAt(currentPosition);
        if (currentChar == '\n' || currentChar == '\r') lineNumber++;
        currentPosition++;
        return currentChar;
    }

    private boolean isAtEnd() {
        return currentPosition >= source.length();
    }

    // Character classification
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // Main lex function
    public ArrayList<Lexeme> lex() {
        while (!isAtEnd()) {
            startOfCurrentLexeme = currentPosition;
            Lexeme nextLexeme = getNextLexeme();
            if (nextLexeme != null) lexemes.add(nextLexeme);
        }
        lexemes.add(new Lexeme(TokenType.EOF, lineNumber));
        return lexemes;
    }

    private Lexeme getNextLexeme() {
        char c = advance();
        switch (c) {
            // Whitespace
            case ' ':
            case '\t':
            case '\n':
            case '\r':
                return null;

            // Strictly one character
            case '‼':
                return new Lexeme(TokenType.BANGBANG, lineNumber);
            case '{':
                return new Lexeme(TokenType.OPEN_CURLY, lineNumber);
            case '}':
                return new Lexeme(TokenType.CLOSED_CURLY, lineNumber);
            case '|':
                return new Lexeme(TokenType.VERTICAL_PIPE, lineNumber);
            case '[':
                return new Lexeme(TokenType.OPEN_SQUARE, lineNumber);
            case ']':
                return new Lexeme(TokenType.CLOSED_SQUARE, lineNumber);
            case '(':
                return new Lexeme(TokenType.OPEN_PAREN, lineNumber);
            case ')':
                return new Lexeme(TokenType.CLOSED_PAREN, lineNumber);
            case '≥':
                return new Lexeme(TokenType.GEQ, lineNumber);
            case '≤':
                return new Lexeme(TokenType.LEQ, lineNumber);
            case '~':
                return new Lexeme(TokenType.APPROX, lineNumber);

            // Multiple characters
            case '+':
                if (match('+')) return new Lexeme(TokenType.INCREMENT, lineNumber);
                if (match('<')) {
                    if (match('-')) return new Lexeme(TokenType.PLUS_ASSIGNMENT, lineNumber);
                }
                return new Lexeme(TokenType.PLUS, lineNumber);
            case '-':
                if (match('-')) return new Lexeme(TokenType.DECREMENT, lineNumber);
                if (match('<')) {
                    if (match('-')) return new Lexeme(TokenType.MINUS_ASSIGNMENT, lineNumber);
                }
                return new Lexeme(TokenType.MINUS, lineNumber);
            case '*':
                if (match('<')) {
                    if (match('-')) return new Lexeme(TokenType.TIMES_ASSIGNMENT, lineNumber);
                }
                return new Lexeme(TokenType.TIMES, lineNumber);
            case '/':
                if (match('/')) return new Lexeme(TokenType.DOUBLE_DIVIDE, lineNumber);
                if (match('<')) {
                    if (match('-')) return new Lexeme(TokenType.DIVIDE_ASSIGNMENT, lineNumber);
                }
                return new Lexeme(TokenType.DIVIDE, lineNumber);
            case '^':
                if (match('<')) {
                    if (match('-')) return new Lexeme(TokenType.CARET_ASSIGNMENT, lineNumber);
                }
                return new Lexeme(TokenType.CARET, lineNumber);
            case '%':
                if (match('<')) {
                    if (match('-')) return new Lexeme(TokenType.PERCENT_ASSIGNMENT, lineNumber);
                }
                return new Lexeme(TokenType.PERCENT, lineNumber);
            case '?':
                if (match('?')) return new Lexeme(TokenType.DOUBLE_QUESTION, lineNumber);
                return new Lexeme(TokenType.QUESTION, lineNumber);
            case '>':
                if (match('?')) return new Lexeme(TokenType.GREATER_QUESTION, lineNumber);
                return new Lexeme(TokenType.GREATER, lineNumber);
            case '<':
                if (match('-')) return new Lexeme(TokenType.ASSIGNMENT, lineNumber);
                if (match('?')) return new Lexeme(TokenType.LESS_QUESTION, lineNumber);
                return new Lexeme(TokenType.LESS, lineNumber);
            case '\\':
                if (match('.')) return new Lexeme(TokenType.BACKSLASH_PERIOD, lineNumber);
                return new Lexeme(TokenType.BACKSLASH, lineNumber);
            case '.':
                if (match('\\')) return new Lexeme(TokenType.PERIOD_BACKSLASH, lineNumber);
                return new Lexeme(TokenType.PERIOD, lineNumber);
            case '!':
                if (match('?')) {
                    if (match('?')) {
                        return new Lexeme(TokenType.NOT_DOUBLE_QUESTION, lineNumber);
                    }
                    return new Lexeme(TokenType.NOT_QUESTION, lineNumber);
                }
                if (match('~')) return new Lexeme(TokenType.NOT_APPROX, lineNumber);
                break;

            // Strings
            case '"':
                return lexString();

            default:
                if (isDigit(c)) return lexNumber();
                else if (isAlpha(c)) return lexIdentifierOrKeyword();
        }
        return null;
    }

    // Lex helpers
    // TODO fix multiple decimal points
    private Lexeme lexNumber() {
        /*boolean hasDecimal = false;
        int position = currentPosition;
        while (true) {
            if (isDigit(peek()) && peek() !== '.') {
                advance();
            } else if (peek() == '.' && !hasDecimal) {
                if (!hasDecimal) {
                    hasDecimal = true;
                    advance();
                } else {
                    error("Malformed real number (has too many decimal points");
                }
            } else if
        }*/
        while (isDigit(peek())) advance();
        if (peek() == '.') {
            if (!isDigit(peekNext())) {
                Sigma.syntaxError("Malformed real number (ends in decimal point)", lineNumber);
                advance();
                return null;
            }
            advance();
            while (isDigit(peek())) advance();
        }
        String target = source.substring(startOfCurrentLexeme, currentPosition);
        double number = Double.parseDouble(target);
        return new Lexeme(TokenType.NUMBER, lineNumber, number);
    }

    private Lexeme lexString() {
        while (true) {
            switch (peek()) {
                case '\0':
                    Sigma.syntaxError("Malformed string (not closed properly)", lineNumber);
                    return null;
                case '"':
                    String target = source.substring(startOfCurrentLexeme + 1, currentPosition);
                    advance();
                    return new Lexeme(TokenType.STRING, lineNumber, target);
                default:
                    advance();
            }
        }
    }

    private Lexeme lexIdentifierOrKeyword() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(startOfCurrentLexeme, currentPosition);
        TokenType type = keywords.get(text);

        if (type == null) {
            return new Lexeme(TokenType.IDENTIFIER, lineNumber, text);
        } else {
            return new Lexeme(type, lineNumber);
        }
    }

    // Print
    public void printLexemes() {
        System.out.println("Lexemes found: ");
        for (Lexeme lexeme : this.lexemes) {
            System.out.println(lexeme);
        }
    }
}
