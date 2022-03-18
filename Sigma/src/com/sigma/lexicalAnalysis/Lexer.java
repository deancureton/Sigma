package com.sigma.lexicalAnalysis;

import com.sigma.Sigma;

import java.util.ArrayList;
import java.util.HashMap;

import static com.sigma.lexicalAnalysis.TokenType.*;


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
        keywords.put("num", NUM_KEYWORD);
        keywords.put("str", STR_KEYWORD);
        keywords.put("tf", TF_KEYWORD);
        keywords.put("var", VAR_KEYWORD);
        keywords.put("func", FUNC_KEYWORD);
        keywords.put("arr", ARR_KEYWORD);
        keywords.put("for", FOR_KEYWORD);
        keywords.put("foreach", FOREACH_KEYWORD);
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
        lexemes.add(new Lexeme(EOF, lineNumber));
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
                return new Lexeme(BANGBANG, lineNumber);
            case '{':
                return new Lexeme(OPEN_CURLY, lineNumber);
            case '}':
                return new Lexeme(CLOSED_CURLY, lineNumber);
            case '»':
                return new Lexeme(DOUBLE_FORWARD, lineNumber);
            case '«':
                return new Lexeme(DOUBLE_BACKWARD, lineNumber);
            case '(':
                return new Lexeme(OPEN_PAREN, lineNumber);
            case ')':
                return new Lexeme(CLOSED_PAREN, lineNumber);
            case '≥':
                return new Lexeme(GEQ, lineNumber);
            case '≤':
                return new Lexeme(LEQ, lineNumber);
            case '~':
                return new Lexeme(APPROX, lineNumber);

            // Multiple characters
            case '+':
                if (match('+')) return new Lexeme(INCREMENT, lineNumber);
                if (match('<')) {
                    if (match('-')) return new Lexeme(PLUS_ASSIGNMENT, lineNumber);
                }
                return new Lexeme(PLUS, lineNumber);
            case '-':
                if (match('-')) return new Lexeme(DECREMENT, lineNumber);
                if (match('<')) {
                    if (match('-')) return new Lexeme(MINUS_ASSIGNMENT, lineNumber);
                }
                return new Lexeme(MINUS, lineNumber);
            case '*':
                if (match('<')) {
                    if (match('-')) return new Lexeme(TIMES_ASSIGNMENT, lineNumber);
                }
                return new Lexeme(TIMES, lineNumber);
            case '/':
                if (match('/')) {
                    if (match('<') && match('-')) {
                        return new Lexeme(DOUBLE_DIVIDE_ASSIGNMENT, lineNumber);
                    }
                    return new Lexeme(DOUBLE_DIVIDE, lineNumber);
                }
                if (match('<')) {
                    if (match('-')) return new Lexeme(DIVIDE_ASSIGNMENT, lineNumber);
                }
                return new Lexeme(DIVIDE, lineNumber);
            case '^':
                if (match('<')) {
                    if (match('-')) return new Lexeme(CARET_ASSIGNMENT, lineNumber);
                }
                return new Lexeme(CARET, lineNumber);
            case '%':
                if (match('<')) {
                    if (match('-')) return new Lexeme(PERCENT_ASSIGNMENT, lineNumber);
                }
                return new Lexeme(PERCENT, lineNumber);
            case '?':
                if (match('?')) return new Lexeme(DOUBLE_QUESTION, lineNumber);
                return new Lexeme(QUESTION, lineNumber);
            case '>':
                if (match('?')) return new Lexeme(GREATER_QUESTION, lineNumber);
                return new Lexeme(GREATER, lineNumber);
            case '<':
                if (match('-')) return new Lexeme(ASSIGN_OPERATOR, lineNumber);
                if (match('?')) return new Lexeme(LESS_QUESTION, lineNumber);
                return new Lexeme(LESS, lineNumber);
            case '\\':
                return lexComment();
            case '.':
                if (match('\\')) return new Lexeme(PERIOD_BACKSLASH, lineNumber);
                return new Lexeme(PERIOD, lineNumber);
            case '!':
                if (match('?')) {
                    if (match('?')) {
                        return new Lexeme(NOT_DOUBLE_QUESTION, lineNumber);
                    }
                    return new Lexeme(NOT_QUESTION, lineNumber);
                }
                if (match('~')) return new Lexeme(NOT_APPROX, lineNumber);
                return new Lexeme(EXCLAMATION, lineNumber);
            case '=':
                Sigma.syntaxError("Equals sign", lineNumber);
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
    private Lexeme lexNumber() {
        boolean hasDecimal = false;
        boolean hasError = false;
        while (true) {
            if (isDigit(peek())) {
                advance();
            } else if (peek() == '.') {
                if (!isDigit(peekNext())) {
                    Sigma.syntaxError("Malformed real number (ends in decimal point)", lineNumber);
                    advance();
                    return null;
                }
                if (!hasDecimal) {
                    hasDecimal = true;
                    advance();
                } else {
                    hasError = true;
                    advance();
                }
            } else {
                break;
            }
        }
        if (hasError) {
            Sigma.syntaxError("Malformed real number (too many decimal points)", lineNumber);
            advance();
            return null;
        }
        String target = source.substring(startOfCurrentLexeme, currentPosition);
        double number = Double.parseDouble(target);
        return new Lexeme(NUMBER, lineNumber, number);
    }

    private Lexeme lexString() {
        while (true) {
            switch (peek()) {
                case '\0' -> {
                    Sigma.syntaxError("Malformed string (not closed properly)", lineNumber);
                    return null;
                }
                case '"' -> {
                    String target = source.substring(startOfCurrentLexeme + 1, currentPosition);
                    advance();
                    return new Lexeme(STRING, lineNumber, target);
                }
                default -> advance();
            }
        }
    }

    private Lexeme lexIdentifierOrKeyword() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(startOfCurrentLexeme, currentPosition);
        TokenType type = keywords.get(text);

        if (type == null) {
            return new Lexeme(IDENTIFIER, lineNumber, text);
        } else {
            return new Lexeme(type, lineNumber);
        }
    }

    private Lexeme lexComment() {
        if (peek() == '.') {
            while (true) {
                advance();
                if (peek() == '.' && peekNext() == '\\') {
                    advance();
                    advance();
                    String target = source.substring(startOfCurrentLexeme, currentPosition);
                    return new Lexeme(COMMENT, lineNumber, target);
                } else if (peek() == '\0') {
                    Sigma.syntaxError("Unclosed comment", lineNumber);
                    return null;
                }
            }
        } else {
            while (true) {
                if (peek() == '\n' || peek() == '\0') {
                    String target = source.substring(startOfCurrentLexeme, currentPosition);
                    return new Lexeme(COMMENT, lineNumber, target);
                }
                advance();
            }
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
