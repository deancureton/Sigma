package com.sigma.lexicalAnalysis;

import com.sigma.environments.Environment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

public class Lexeme {
    // Instance Variables
    private TokenType type;
    private final Integer lineNumber;

    private String stringVal;
    private Double numVal; // Sigma only supports a single real number type
    private Boolean boolVal;
    public ArrayList<Lexeme> arrayVal;

    private final ArrayList<Lexeme> children = new ArrayList<>();

    private Environment definingEnvironment;

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

    public Lexeme(TokenType type, int lineNumber, ArrayList<Lexeme> arrayVal) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.arrayVal = arrayVal;
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

    public Environment getDefiningEnvironment() {
        return definingEnvironment;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public void setStringVal(String stringVal) {
        this.stringVal = stringVal;
    }

    public void setNumVal(Double numVal) {
        this.numVal = numVal;
    }

    public void setBoolVal(Boolean boolVal) {
        this.boolVal = boolVal;
    }

    public void setDefiningEnvironment(Environment definingEnvironment) {
        this.definingEnvironment = definingEnvironment;
    }

    // Parse tree
    public void addChild(Lexeme child) {
        this.children.add(child);
    }

    public Lexeme getChild(int index) {
        return this.children.get(index);
    }

    public int getNumChildren() {
        return this.children.size();
    }

    // Equality
    public boolean equals(Lexeme compare) {
        boolean result = true;
        if (compare.getType() != this.getType()) result = false;
        if (!Objects.equals(compare.getStringVal(), this.getStringVal())) result = false;
        if (!Objects.equals(compare.getNumVal(), this.getNumVal())) result = false;
        if (!Objects.equals(compare.getBoolVal(), this.getBoolVal())) result = false;
        if (this.arrayVal != null) {
            if (!(this.arrayVal.containsAll(compare.arrayVal) && compare.arrayVal.containsAll(this.arrayVal)))
                result = false;
        }
        return result;
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
            if (!getBoolVal()) {
                output += ": fals";
            } else {
                output += ": " + getBoolVal();
            }
        } else if (arrayVal != null) {
            output += ": " + arrayVal.toString();
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
        System.out.print("\n");
    }
}