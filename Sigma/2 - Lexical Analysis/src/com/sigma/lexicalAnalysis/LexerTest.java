package com.sigma.lexicalAnalysis;

public class LexerTest {
    public static void main(String[] args) {
        Lexeme numberKeyword = new Lexeme(TokenType.NUM_KEYWORD, 1);
        Lexeme x = new Lexeme(TokenType.IDENTIFIER, 1, "x");
        Lexeme assignment = new Lexeme(TokenType.ASSIGNMENT, 1);
        Lexeme ten = new Lexeme(TokenType.NUMBER, 1, 10);
        Lexeme semi = new Lexeme(TokenType.BANGBANG, 1);
        System.out.println(numberKeyword);
        System.out.println(x);
        System.out.println(assignment);
        System.out.println(ten);
        System.out.println(semi);
    }
}