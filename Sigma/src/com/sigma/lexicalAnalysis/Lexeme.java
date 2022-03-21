package com.sigma.lexicalAnalysis;

import java.util.ArrayList;

public class Lexeme {
    // Instance Variables
    private final TokenType type;
    private final Integer lineNumber;

    private String stringVal;
    private Double numVal; // Sigma only supports a single real number type
    private Boolean boolVal;

    private final ArrayList<Lexeme> children = new ArrayList<Lexeme>();

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

    public void addChild(Lexeme child) {
        this.children.add(child);
    }

    public Lexeme getChild(int index) {
        return this.children.get(index);
    }

    // toString
    public String toString() {
        String output = "[" + getType() + "] (line " + getLineNumber() + ")";
        if (getStringVal() != null) {
            if (getType() == TokenType.STRING) {
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

    public void printTree(Lexeme root, int indents) {
        System.out.print(root);
        for (Lexeme lexeme : root.children) {
            if (lexeme != null) {
                System.out.print('\n');
                for (int i = 0; i < indents + 1; i++) {
                    System.out.print('\t');
                }
                printTree(lexeme, indents + 1);
            }
        }
    }

    public void printTree() {
        printTree(this, 0);
    }
}