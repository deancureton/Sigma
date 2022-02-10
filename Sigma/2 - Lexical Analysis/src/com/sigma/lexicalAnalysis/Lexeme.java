package com.sigma.lexicalAnalysis;

public class Lexeme {
    private TokenType type;
    private Integer lineNumber;

    private String stringVal;
    private Double numVal; // Sigma only supports a single real number type
    private Boolean boolVal;

    public Lexeme(TokenType lexemeType, int lexemeLineNumber) {
        type = lexemeType;
        lineNumber = lexemeLineNumber;
    }

    public Lexeme(TokenType lexemeType, int lexemeLineNumber, String lexemeValue) {
        stringVal = lexemeValue;
        type = lexemeType;
        lineNumber = lexemeLineNumber;
    }

    public Lexeme(TokenType lexemeType, int lexemeLineNumber, double lexemeValue) {
        numVal = lexemeValue;
        type = lexemeType;
        lineNumber = lexemeLineNumber;
    }

    public Lexeme(TokenType lexemeType, int lexemeLineNumber, boolean lexemeValue) {
        boolVal = lexemeValue;
        type = lexemeType;
        lineNumber = lexemeLineNumber;
    }

    public TokenType getType() {
        return type;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public String getStringVal() {
        return stringVal;
    }

    public Double getNumVal() {
        return numVal;
    }

    public Boolean getBoolVal() {
        return boolVal;
    }

    public String toString() {
        String output = "[" + getType() + "] (line " + getLineNumber() + ")";
        if (getStringVal() != null) {
            output += ": " + getStringVal();
            return output;
        }
        if (getNumVal() != null) {
            output += ": " + getNumVal();
            return output;
        }
        if (getBoolVal() != null) {
            output += ": " + getBoolVal();
            return output;
        }
        return output;
    }
}