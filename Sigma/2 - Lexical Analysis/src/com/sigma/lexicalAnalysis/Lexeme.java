package com.sigma.lexicalAnalysis;

import static com.sigma.lexicalAnalysis.TokenType.*;

public class Lexeme {
    // Instance Variables
    private TokenType type;
    private Integer lineNumber;

    private String stringVal;
    private Double numVal; // Sigma only supports a single real number type
    private Boolean boolVal;

    // Constructors
    public Lexeme(TokenType type, int lineNumber) {
        this.type = type;
        this.lineNumber = lineNumber;
    }

    public Lexeme(TokenType type, int lineNumber, String stringVal) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.stringVal = stringVal;
    }

    public Lexeme(TokenType type, int lineNumber, double numVal) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.numVal = numVal;
    }

    public Lexeme(TokenType type, int lineNumber, boolean boolVal) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.boolVal = boolVal;
    }

    // Getters
    public TokenType getType() {
        return this.type;
    }

    public Integer getLineNumber() {
        return this.lineNumber;
    }

    public String getStringVal() {
        return this.stringVal;
    }

    public Double getNumVal() {
        return this.numVal;
    }

    public Boolean getBoolVal() {
        return this.boolVal;
    }

    // toString
    public String toString() {
        String output = "[" + getType() + "] (line " + getLineNumber() + ")";
        if (getStringVal() != null) {
            if (getType() == STRING) {
                output += ": \"" + getStringVal() + "\"";
            } else {
                output += ": " + getStringVal();
            }
        } else if (getNumVal() != null) {
            output += ": " + getNumVal();
        } else if (getBoolVal() != null) {
            output += ": " + getBoolVal();
        }
        return output;
    }
}