package com.sigma.lexicalAnalysis;

import static com.sigma.lexicalAnalysis.TokenType.*;

public class LexerTest {
    public static void main(String[] args) {
        /*Lexeme numberKeyword = new Lexeme(NUM_KEYWORD, 1);
        Lexeme x = new Lexeme(IDENTIFIER, 1, "x");
        Lexeme assignment = new Lexeme(ASSIGNMENT, 1);
        Lexeme ten = new Lexeme(NUMBER, 1, 10);
        Lexeme semi = new Lexeme(BANGBANG, 1);
        System.out.println(numberKeyword);
        System.out.println(x);
        System.out.println(assignment);
        System.out.println(ten);
        System.out.println(semi);*/
        Lexer lexer = new Lexer("int x = 10;");
        lexer.lex();
        lexer.printLexemes();
    }
}