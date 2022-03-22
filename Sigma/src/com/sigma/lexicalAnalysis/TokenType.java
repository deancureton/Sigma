package com.sigma.lexicalAnalysis;

public enum TokenType {
    // One character
    BANGBANG, OPEN_CURLY, CLOSED_CURLY, DOUBLE_FORWARD, DOUBLE_BACKWARD, OPEN_PAREN, CLOSED_PAREN, OPEN_SQUARE, CLOSED_SQUARE,
    PLUS, MINUS, TIMES, DIVIDE,
    CARET, PERCENT,
    EXCLAMATION,
    QUESTION, APPROX, GREATER, LESS, GEQ, LEQ,
    PERIOD,
    VERTICAL_BAR,

    // Two characters
    ASSIGN_OPERATOR,
    NOT_QUESTION, NOT_APPROX,
    DOUBLE_QUESTION, GREATER_QUESTION, LESS_QUESTION,
    DOUBLE_DIVIDE,
    INCREMENT, DECREMENT,

    // Three or more characters
    NOT_DOUBLE_QUESTION,
    PLUS_ASSIGNMENT, MINUS_ASSIGNMENT, TIMES_ASSIGNMENT, DIVIDE_ASSIGNMENT, DOUBLE_DIVIDE_ASSIGNMENT,
    CARET_ASSIGNMENT, PERCENT_ASSIGNMENT,
    COMMENT,

    // Literals
    IDENTIFIER, NUMBER, STRING,

    // Keywords
    NUM_KEYWORD, STR_KEYWORD, TF_KEYWORD, VAR_KEYWORD, FUNC_KEYWORD, ARR_KEYWORD,
    FOR_KEYWORD, FOREACH_KEYWORD, WHEN_KEYWORD, LOOP_KEYWORD,
    OF_KEYWORD, COUNT_KEYWORD,
    NOTHING_KEYWORD,
    AND_KEYWORD, OR_KEYWORD, NOT_KEYWORD,
    NAND_KEYWORD, NOR_KEYWORD, XOR_KEYWORD, XNOR_KEYWORD, IMPLIES_KEYWORD,
    IF_KEYWORD, BUTIF_KEYWORD, BUT_KEYWORD,
    CHANGE_KEYWORD, CASE_KEYWORD,
    TRUE_KEYWORD, FALS_KEYWORD,

    // End of file
    EOF,

    // Program lexemes
    PROGRAM,
    STATEMENT_LIST,
    VARIABLE_DECLARATION, FUNCTION_DEFINITION,
    ASSIGNMENT, REGULAR_ASSIGNMENT,
    IF_STATEMENT, BUTIF_STATEMENT_LIST, BUTIF_STATEMENT, BUT_STATEMENT,
    CHANGE_STATEMENT, CHANGE_CASES, CHANGE_CASE,
    FOR_LOOP, FOREACH_LOOP, WHEN_LOOP, LOOP_LOOP,
    BINARY_EXPRESSION, UNARY_EXPRESSION,
    CAST, FUNCTION_CALL, CALL_ARGUMENTS, FUNCTION_ARGS, OPTIONAL_FUNCTION_ARGS,
    ARRAY_ELEMENTS,
    GLUE,
    SENTINEL
}