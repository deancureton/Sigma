package com.sigma.recognizing;

import com.sigma.Sigma;
import com.sigma.lexicalAnalysis.Lexeme;
import com.sigma.lexicalAnalysis.TokenType;

import javax.swing.text.Caret;
import java.util.ArrayList;

import static com.sigma.lexicalAnalysis.TokenType.*;

public class Recognizer {
    private static final boolean printDebugMessages = true;
    private final ArrayList<Lexeme> lexemes;
    private Lexeme currentLexeme;
    private int nextLexemeIndex;

    // Constructor
    public Recognizer(ArrayList<Lexeme> lexemes) {
        this.lexemes = lexemes;
        this.nextLexemeIndex = 0;
        advance();
    }

    // Support methods
    private boolean check(TokenType type) {
        return currentLexeme.getType() == type;
    }

    private boolean checkNext(TokenType type) {
        if (nextLexemeIndex >= lexemes.size()) return false;
        return lexemes.get(nextLexemeIndex).getType() == type;
    }

    private void consume(TokenType expected) {
        if (check(expected)) {
            advance();
            if (printDebugMessages) System.out.println("-- " + expected + " --");
        } else error("Expected " + expected + " but found " + currentLexeme + ".");
    }

    private void advance() {
        currentLexeme = lexemes.get(nextLexemeIndex);
        nextLexemeIndex++;
    }

    // Consumption functions
    public void program() {
        if (printDebugMessages) System.out.println("-- program --");
        if (statementListPending()) statementList();
    }

    private void statementList() {
        if (printDebugMessages) System.out.println("-- statementList --");
        statement();
        while (statementPending()) statement();
    }

    private void statement() {
        if (printDebugMessages) System.out.println("-- statement --");
        if (variableDeclarationPending()) {
            variableDeclaration();
            consume(BANGBANG);
        } else if (assignmentPending()) {
            assignment();
            consume(BANGBANG);
        } else if (functionDefinitionPending()) functionDefinition();
        else if (loopPending()) loop();
        else if (ifStatementPending()) ifStatement();
        else if (commentPending()) comment();
        else if (expressionPending()) {
            expression();
            consume(BANGBANG);
        } else error("Expected statement.");
    }

    private void variableDeclaration() {
        if (printDebugMessages) System.out.println("-- variableDeclaration --");
        consume(VAR_KEYWORD);
        consume(IDENTIFIER);
        if (regularAssignmentPending()) regularAssignment();
    }

    private void assignment() {
        if (printDebugMessages) System.out.println("-- assignment --");
        if (unaryOperatorPending()) {
            unaryOperator();
            consume(IDENTIFIER);
        } else if (check(IDENTIFIER)) {
            consume(IDENTIFIER);
            regularAssignment();
        } else error("Expected assignment operator.");
    }

    private void functionDefinition() {
        if (printDebugMessages) System.out.println("-- functionDefinition --");
        consume(FUNC_KEYWORD);
        consume(IDENTIFIER);
        consume(ASSIGNMENT);
        functionArguments();
        block();
        consume(BANGBANG);
    }

    private void loop() {
        if (printDebugMessages) System.out.println("-- loop --");
        if (forLoopPending()) forLoop();
        else if (foreachLoopPending()) foreachLoop();
        else if (whenLoopPending()) whenLoop();
        else if (loopLoopPending()) loopLoop();
        else error("Expected loop.");
    }

    private void ifStatement() {
        if (printDebugMessages) System.out.println("-- ifStatement --");
        consume(IF_KEYWORD);
        consume(OPEN_CURLY);
        expression();
        consume(CLOSED_CURLY);
        block();
        while (butifStatementPending()) {
            butifStatement();
        }
        if (butStatementPending()) butStatement();
    }

    private void comment() {
        if (printDebugMessages) System.out.println("-- comment --");
        consume(COMMENT);
    }

    private void expression() {
        if (printDebugMessages) System.out.println("-- expression --");
        if (binaryExpressionPending()) binaryExpression();
        else if (primaryPending()) primary();
        else error("Expression expected.");
    }

    private void type() {
        if (printDebugMessages) System.out.println("-- type --");
        if (check(STR_KEYWORD)) consume(STR_KEYWORD);
        else if (check(NUM_KEYWORD)) consume(NUM_KEYWORD);
        else if (check(TF_KEYWORD)) consume(TF_KEYWORD);
        else if (check(ARR_KEYWORD)) consume(ARR_KEYWORD);
        else error("Expected type keyword.");
    }

    private void regularAssignment() {
        if (printDebugMessages) System.out.println("-- regularAssignment --");
        if (check(ASSIGNMENT)) consume(ASSIGNMENT);
        else if (operatorAssignmentPending()) operatorAssignment();
        else error("Expected assignment operator.");
        expression();
    }

    private void unaryOperator() {
        if (printDebugMessages) System.out.println("-- unaryOperator --");
        if (check(INCREMENT)) consume(INCREMENT);
        else if (check(DECREMENT)) consume(DECREMENT);
        else if (check(NOT_KEYWORD)) consume(NOT_KEYWORD);
        else if (check(EXCLAMATION)) consume(EXCLAMATION);
        else error("Expected unary assignment operator.");
    }

    private void functionArguments() {
        if (printDebugMessages) System.out.println("-- functionArguments --");
        while (functionArgumentPending()) functionArgument();
        if (check(OPEN_SQUARE)) {
            consume(OPEN_SQUARE);
            functionArgument();
            while (functionArgumentPending()) functionArgument();
            consume(CLOSED_SQUARE);
        }
    }

    private void block() {
        if (printDebugMessages) System.out.println("-- block --");
        consume(VERTICAL_PIPE);
        while (statementListPending()) {
            statementList();
        }
        consume(VERTICAL_PIPE);
    }

    private void forLoop() {
        if (printDebugMessages) System.out.println("-- forLoop --");
        consume(FOR_KEYWORD);
        consume(OPEN_CURLY);
        variableDeclaration();
        consume(BANGBANG);
        expression();
        consume(BANGBANG);
        assignment();
        consume(CLOSED_CURLY);
        block();
    }

    private void foreachLoop() {
        if (printDebugMessages) System.out.println("-- foreachLoop --");
        consume(FOREACH_KEYWORD);
        consume(OPEN_CURLY);
        consume(VAR_KEYWORD);
        consume(IDENTIFIER);
        consume(OF_KEYWORD);
        consume(IDENTIFIER);
        consume(CLOSED_CURLY);
        block();
    }

    private void whenLoop() {
        if (printDebugMessages) System.out.println("-- whenLoop --");
        consume(WHEN_KEYWORD);
        consume(OPEN_CURLY);
        expression();
        consume(CLOSED_CURLY);
        block();
    }

    private void loopLoop() {
        if (printDebugMessages) System.out.println("-- loopLoop --");
        consume(LOOP_KEYWORD);
        consume(OPEN_CURLY);
        consume(NUMBER);
        consume(CLOSED_CURLY);
        block();
    }

    private void butifStatement() {
        if (printDebugMessages) System.out.println("-- butifStatement --");
        consume(BUTIF_KEYWORD);
        consume(OPEN_CURLY);
        expression();
        consume(CLOSED_CURLY);
        block();
    }

    private void butStatement() {
        if (printDebugMessages) System.out.println("-- butStatement --");
        consume(BUT_KEYWORD);
        consume(OPEN_CURLY);
        expression();
        consume(CLOSED_CURLY);
        block();
    }

    private void binaryExpression() {
        if (printDebugMessages) System.out.println("-- binaryExpression --");
        primary();
        binaryOperator();
        if (binaryExpressionPending()) binaryExpression();
        else if (primaryPending()) primary();
        else error("Expected primary or further expression.");
    }

    private void primary() {
        if (printDebugMessages) System.out.println("-- primary --");
        if (unaryExpressionPending()) unaryExpression();
        else if (check(NUMBER)) consume(NUMBER);
        else if (functionCallPending()) functionCall();
        else if (castPending()) cast();
        else if (check(IDENTIFIER)) consume(IDENTIFIER);
        else if (check(STRING)) consume(STRING);
        else if (booleanPending()) bool();
        else if (arrayPending()) array();
        else error("Expected primary.");
    }

    private void unaryExpression() {
        if (printDebugMessages) System.out.println("-- unaryExpression --");
        if (unaryOperatorPending()) {
            unaryOperator();
            primary();
        } else if (minusExpressionPending()) minusExpression();
        else if (parenthesizedExpressionPending()) parenthesizedExpression();
        else error("Expected unary expression.");
    }

    private void operatorAssignment() {
        if (printDebugMessages) System.out.println("-- operatorAssignment --");
        if (check(PLUS_ASSIGNMENT)) consume(PLUS_ASSIGNMENT);
        else if (check(MINUS_ASSIGNMENT)) consume(MINUS_ASSIGNMENT);
        else if (check(DIVIDE_ASSIGNMENT)) consume(DIVIDE_ASSIGNMENT);
        else if (check(TIMES_ASSIGNMENT)) consume(TIMES_ASSIGNMENT);
        else if (check(DOUBLE_DIVIDE_ASSIGNMENT)) consume(DOUBLE_DIVIDE_ASSIGNMENT);
        else if (check(CARET_ASSIGNMENT)) consume(CARET_ASSIGNMENT);
        else if (check(PERCENT_ASSIGNMENT)) consume(PERCENT_ASSIGNMENT);
        else error("Expected assignment operator.");
    }

    private void functionArgument() {
        if (printDebugMessages) System.out.println("-- functionArgument --");
        consume(VAR_KEYWORD);
        consume(IDENTIFIER);
    }

    private void binaryOperator() {
        if (printDebugMessages) System.out.println("-- binaryOperator --");
        if (binaryArithmeticOperatorPending()) binaryArithmeticOperator();
        else if (binaryComparatorPending()) binaryComparator();
        else if (binaryBooleanOperatorPending()) binaryBooleanOperator();
        else error("Expected binary operator.");
    }

    private void bool() {
        if (printDebugMessages) System.out.println("-- boolean --");
        if (check(TRUE_KEYWORD)) consume(TRUE_KEYWORD);
        else if (check(FALS_KEYWORD)) consume(FALS_KEYWORD);
        else error("Expected boolean.");
    }

    private void array() {
        if (printDebugMessages) System.out.println("-- array --");
        consume(OPEN_PAREN);
        arrayElements();
        consume(CLOSED_PAREN);
    }

    private void cast() {
        if (printDebugMessages) System.out.println("-- cast --");
        consume(IDENTIFIER);
        consume(PERIOD);
        type();
    }

    private void functionCall() {
        if (printDebugMessages) System.out.println("-- functionCall --");
        consume(IDENTIFIER);
        consume(OPEN_CURLY);
        callArguments();
        consume(CLOSED_CURLY);
    }

    private void parenthesizedExpression() {
        if (printDebugMessages) System.out.println("-- parenthesizedExpression --");
        consume(OPEN_CURLY);
        expression();
        consume(CLOSED_CURLY);
    }

    private void binaryArithmeticOperator() {
        if (printDebugMessages) System.out.println("-- binaryArithmeticOperator --");
        if (check(PLUS)) consume(PLUS);
        else if (check(MINUS)) consume(MINUS);
        else if (check(DIVIDE)) consume(DIVIDE);
        else if (check(TIMES)) consume(TIMES);
        else if (check(DOUBLE_DIVIDE)) consume(DOUBLE_DIVIDE);
        else if (check(CARET)) consume(CARET);
        else if (check(PERCENT)) consume(PERCENT);
        else error("Expected binary arithmetic operator.");
    }

    private void binaryComparator() {
        if (printDebugMessages) System.out.println("-- binaryComparator --");
        if (check(GREATER)) consume(GREATER);
        else if (check(LESS)) consume(LESS);
        else if (check(GREATER_QUESTION)) consume(GREATER_QUESTION);
        else if (check(LESS_QUESTION)) consume(LESS_QUESTION);
        else if (check(GEQ)) consume(GEQ);
        else if (check(LEQ)) consume(LEQ);
        else if (check(QUESTION)) consume(QUESTION);
        else if (check(NOT_QUESTION)) consume(NOT_QUESTION);
        else if (check(DOUBLE_QUESTION)) consume(DOUBLE_QUESTION);
        else if (check(NOT_DOUBLE_QUESTION)) consume(NOT_DOUBLE_QUESTION);
        else if (check(APPROX)) consume(APPROX);
        else if (check(NOT_APPROX)) consume(NOT_APPROX);
        else error("Expected binary comparator.");
    }

    private void binaryBooleanOperator() {
        if (printDebugMessages) System.out.println("-- binaryBooleanOperator --");
        if (check(AND_KEYWORD)) consume(AND_KEYWORD);
        else if (check(OR_KEYWORD)) consume(OR_KEYWORD);
        else if (check(NAND_KEYWORD)) consume(NAND_KEYWORD);
        else if (check(NOR_KEYWORD)) consume(NOR_KEYWORD);
        else if (check(XOR_KEYWORD)) consume(XOR_KEYWORD);
        else if (check(XNOR_KEYWORD)) consume(XNOR_KEYWORD);
        else if (check(IMPLIES_KEYWORD)) consume(IMPLIES_KEYWORD);
        else error("Expected boolean operator.");
    }

    private void arrayElements() {
        if (printDebugMessages) System.out.println("-- arrayElements --");
        while (primaryPending()) primary();
    }

    private void callArguments() {
        if (printDebugMessages) System.out.println("-- callArguments --");
        while (primaryPending()) primary();
    }

    private void minusExpression() {
        if (printDebugMessages) System.out.println("-- minusExpression --");
        consume(MINUS);
        expression();
    }

    // Pending functions
    private boolean statementListPending() {
        return statementPending();
    }

    private boolean statementPending() {
        return variableDeclarationPending()
                || assignmentPending()
                || functionDefinitionPending()
                || loopPending()
                || ifStatementPending()
                || commentPending()
                || expressionPending();
    }

    private boolean variableDeclarationPending() {
        return check(VAR_KEYWORD) && checkNext(IDENTIFIER);
    }

    private boolean assignmentPending() {
        return (check(IDENTIFIER) && regularAssignmentPendingNext()) || (unaryOperatorPending() && checkNext(IDENTIFIER));
    }

    private boolean functionDefinitionPending() {
        return check(FUNC_KEYWORD);
    }

    private boolean loopPending() {
        return forLoopPending() || foreachLoopPending() || whenLoopPending() || loopLoopPending();
    }

    private boolean ifStatementPending() {
        return check(IF_KEYWORD);
    }

    private boolean butifStatementPending() {
        return check(BUTIF_KEYWORD);
    }

    private boolean butStatementPending() {
        return check(BUT_KEYWORD);
    }

    private boolean commentPending() {
        return check(COMMENT);
    }

    private boolean expressionPending() {
        return binaryExpressionPending() || primaryPending();
    }

    private boolean typePending() {
        return check(VAR_KEYWORD) || strictTypePending();
    }

    private boolean strictTypePending() {
        return check(STR_KEYWORD) || check(NUM_KEYWORD) || check(TF_KEYWORD) || check(ARR_KEYWORD);
    }

    private boolean unaryOperatorPending() {
        return check(INCREMENT) || check(DECREMENT) || check(NOT_KEYWORD) || check(EXCLAMATION);
    }

    private boolean regularAssignmentPending() {
        return check(ASSIGNMENT) || operatorAssignmentPending();
    }

    private boolean regularAssignmentPendingNext() {
        return checkNext(ASSIGNMENT) || operatorAssignmentPendingNext();
    }

    private boolean forLoopPending() {
        return check(FOR_KEYWORD);
    }

    private boolean foreachLoopPending() {
        return check(FOREACH_KEYWORD);
    }

    private boolean whenLoopPending() {
        return check(WHEN_KEYWORD);
    }

    private boolean loopLoopPending() {
        return check(LOOP_KEYWORD);
    }

    private boolean binaryExpressionPending() {
        return primaryPending() && binaryOperatorPendingNext();
    }

    private boolean primaryPending() {
        return unaryExpressionPending() || check(NUMBER) || check(IDENTIFIER) || check(STRING) || booleanPending() || arrayPending() || castPending() || functionCallPending();
    }

    private boolean unaryExpressionPending() {
        return unaryOperatorPending() || minusExpressionPending() || parenthesizedExpressionPending();
    }

    private boolean operatorAssignmentPending() {
        return check(PLUS_ASSIGNMENT)
                || check(MINUS_ASSIGNMENT)
                || check(DIVIDE_ASSIGNMENT)
                || check(TIMES_ASSIGNMENT)
                || check(DOUBLE_DIVIDE_ASSIGNMENT)
                || check(CARET_ASSIGNMENT)
                || check(PERCENT_ASSIGNMENT);
    }

    private boolean operatorAssignmentPendingNext() {
        return checkNext(PLUS_ASSIGNMENT)
                || checkNext(MINUS_ASSIGNMENT)
                || checkNext(DIVIDE_ASSIGNMENT)
                || checkNext(TIMES_ASSIGNMENT)
                || checkNext(DOUBLE_DIVIDE_ASSIGNMENT)
                || checkNext(CARET_ASSIGNMENT)
                || checkNext(PERCENT_ASSIGNMENT);
    }

    private boolean binaryOperatorPendingNext() {
        return binaryArithmeticOperatorPendingNext() || binaryComparatorPendingNext() || binaryBooleanOperatorPendingNext();
    }

    private boolean binaryArithmeticOperatorPending() {
        return check(PLUS)
                || check(MINUS)
                || check(DIVIDE)
                || check(TIMES)
                || check(DOUBLE_DIVIDE)
                || check(CARET)
                || check(PERCENT);
    }

    private boolean binaryComparatorPending() {
        return check(GREATER)
                || check(LESS)
                || check(GREATER_QUESTION)
                || check(LESS_QUESTION)
                || check(GEQ)
                || check(LEQ)
                || check(QUESTION)
                || check(NOT_QUESTION)
                || check(DOUBLE_QUESTION)
                || check(NOT_DOUBLE_QUESTION)
                || check(APPROX)
                || check(NOT_APPROX);
    }

    private boolean binaryBooleanOperatorPending() {
        return check(AND_KEYWORD)
                || check(OR_KEYWORD)
                || check(NOT_KEYWORD)
                || check(NAND_KEYWORD)
                || check(NOR_KEYWORD)
                || check(XOR_KEYWORD)
                || check(XNOR_KEYWORD)
                || check(IMPLIES_KEYWORD);
    }

    private boolean binaryArithmeticOperatorPendingNext() {
        return checkNext(PLUS)
                || checkNext(MINUS)
                || checkNext(DIVIDE)
                || checkNext(TIMES)
                || checkNext(DOUBLE_DIVIDE)
                || checkNext(CARET)
                || checkNext(PERCENT);
    }

    private boolean binaryComparatorPendingNext() {
        return checkNext(GREATER)
                || checkNext(LESS)
                || checkNext(GREATER_QUESTION)
                || checkNext(LESS_QUESTION)
                || checkNext(GEQ)
                || checkNext(LEQ)
                || checkNext(QUESTION)
                || checkNext(NOT_QUESTION)
                || checkNext(DOUBLE_QUESTION)
                || checkNext(NOT_DOUBLE_QUESTION)
                || checkNext(APPROX)
                || checkNext(NOT_APPROX);
    }

    private boolean binaryBooleanOperatorPendingNext() {
        return checkNext(AND_KEYWORD)
                || checkNext(OR_KEYWORD)
                || checkNext(NOT_KEYWORD)
                || checkNext(NAND_KEYWORD)
                || checkNext(NOR_KEYWORD)
                || checkNext(XOR_KEYWORD)
                || checkNext(XNOR_KEYWORD)
                || checkNext(IMPLIES_KEYWORD);
    }

    private boolean booleanPending() {
        return check(TRUE_KEYWORD) || check(FALS_KEYWORD);
    }

    private boolean arrayPending() {
        return check(OPEN_PAREN);
    }

    private boolean castPending() {
        return check(IDENTIFIER) && checkNext(PERIOD);
    }

    private boolean functionCallPending() {
        return check(IDENTIFIER) && checkNext(OPEN_CURLY);
    }

    private boolean functionArgumentPending() {
        return typePending() && checkNext(IDENTIFIER);
    }

    private boolean minusExpressionPending() {
        return check(MINUS);
    }

    private boolean parenthesizedExpressionPending() {
        return check(OPEN_CURLY);
    }

    // Debugging
    private static void log(String message) {
        if (printDebugMessages) System.out.println(message);
    }

    // Error reporting
    private void error(String message) {
        Sigma.syntaxError(message, currentLexeme);
    }
}