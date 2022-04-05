package com.sigma.environments;

import com.sigma.lexicalAnalysis.Lexeme;
import com.sigma.lexicalAnalysis.TokenType;

import java.util.Objects;

public class EnvironmentTest {
    public static void main(String[] args) {
        Environment global = new Environment();
        Environment local1 = new Environment(global);
        Environment local2 = new Environment(global);
        Environment local3 = new Environment(local1);
        Lexeme a = new Lexeme(TokenType.IDENTIFIER, 1, "a");
        Lexeme aVal = new Lexeme(TokenType.STRING, 1, "avalue");
        Lexeme b = new Lexeme(TokenType.IDENTIFIER, 1, "b");
        Lexeme bVal = new Lexeme(TokenType.STRING, 1, "bvalue");
        Lexeme c = new Lexeme(TokenType.IDENTIFIER, 1, "c");
        Lexeme cVal = new Lexeme(TokenType.STRING, 1, "cvalue");
        Lexeme d = new Lexeme(TokenType.IDENTIFIER, 1, "d");
        Lexeme dVal = new Lexeme(TokenType.STRING, 1, "dvalue");
        global.add(a, aVal);
        local1.add(b, bVal);
        local3.add(c, cVal);
        local2.add(d, dVal);
        System.out.println(global.toString());
        System.out.println(local1.toString());
        global.update(a, new Lexeme(TokenType.STRING, 1, "avalue2"));
        System.out.println(local3.lookup(a));
        System.out.println(local2.lookup(d));
        System.out.println(local1.lookup(b));
        System.out.println(local1.lookup(c));
    }
}