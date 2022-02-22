package com.sigma.lexicalAnalysis;

public enum TokenType {
    // One character
    BANGBANG, OPEN_CURLY, CLOSED_CURLY, VERTICAL_PIPE, OPEN_SQUARE, CLOSED_SQUARE, OPEN_PAREN, CLOSED_PAREN,
    PLUS, MINUS, TIMES, DIVIDE,
    CARET, PERCENT,
    EXCLAMATION,
    QUESTION, APPROX, GREATER, LESS, GEQ, LEQ,
    PERIOD,
    BACKSLASH,

    // Two characters
    ASSIGNMENT,
    NOT_QUESTION, NOT_APPROX,
    DOUBLE_QUESTION, GREATER_QUESTION, LESS_QUESTION,
    DOUBLE_DIVIDE,
    INCREMENT, DECREMENT,
    BACKSLASH_PERIOD, PERIOD_BACKSLASH,

    // Three or more characters
    NOT_DOUBLE_QUESTION,
    PLUS_ASSIGNMENT, MINUS_ASSIGNMENT, TIMES_ASSIGNMENT, DIVIDE_ASSIGNMENT, DOUBLE_DIVIDE_ASSIGNMENT,
    CARET_ASSIGNMENT, PERCENT_ASSIGNMENT,

    // Literals
    IDENTIFIER, NUMBER, STRING,

    // Keywords
    NUM_KEYWORD, STR_KEYWORD, TF_KEYWORD, VAR_KEYWORD, FUNC_KEYWORD, ARR_KEYWORD,
    FOR_KEYWORD, FOREACH_KEYWORD, WHEN_KEYWORD, LOOP_KEYWORD, CHANGE_KEYWORD,
    OF_KEYWORD, COUNT_KEYWORD,
    NOTHING_KEYWORD,
    AND_KEYWORD, OR_KEYWORD, NOT_KEYWORD,
    NAND_KEYWORD, NOR_KEYWORD, XOR_KEYWORD, XNOR_KEYWORD, IMPLIES_KEYWORD,
    IF_KEYWORD, BUTIF_KEYWORD, BUT_KEYWORD,
    TRUE_KEYWORD, FALS_KEYWORD,

    // End of file
    EOF
}