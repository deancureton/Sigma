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
        if (check(expected)) advance();
        else error("Expected " + expected + " but found " + currentLexeme + ".");
    }

    private void advance() {
        currentLexeme = lexemes.get(nextLexemeIndex);
        nextLexemeIndex++;
    }

    // Pending functions
    private boolean statementListPending() {
        return statementPending();
    }

    private boolean statementPending() {
        return variableDeclarationPending()
                || variableInitializationPending()
                || assignmentPending()
                || functionDefinitionPending()
                || loopPending()
                || ifStatementPending()
                || commentPending()
                || expressionPending();
    }

    private boolean variableDeclarationPending() {
        return typePending() && checkNext(IDENTIFIER);
    }

    private boolean variableInitializationPending() {
        return strictTypePending();
    }

    private boolean assignmentPending() {
        return unaryAssignmentPending() || regularAssignmentPending();
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

    private boolean commentPending() {
        return singleLineCommentPending() || multiLineCommentPending();
    }

    private boolean expressionPending() {
        return binaryExpressionPending() || primaryPending() || unaryExpressionPending();
    }

    private boolean typePending() {
        return check(VAR_KEYWORD) || strictTypePending();
    }

    private boolean strictTypePending() {
        return check(STR_KEYWORD) || check(NUM_KEYWORD) || check(TF_KEYWORD) || check(ARR_KEYWORD);
    }

    private boolean unaryAssignmentPending() {
        return incrementPending() || decrementPending();
    }

    private boolean regularAssignmentPending() {
        return check(IDENTIFIER) && (checkNext(ASSIGNMENT) || operatorAssignmentPendingNext());
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

    private boolean singleLineCommentPending() {
        return check(BACKSLASH);
    }

    private boolean multiLineCommentPending() {
        return check(BACKSLASH_PERIOD);
    }

    private boolean binaryExpressionPending() {
        return primaryPending() && binaryOperatorPendingNext();
    }

    private boolean primaryPending() {
        return check(NUMBER) || check(IDENTIFIER) || check(STRING) || booleanPending() || unaryExpressionPending() || arrayPending() || castPending() || functionCallPending();
    }

    private boolean unaryExpressionPending() {
        return unaryAssignmentPending() || negationExpressionPending() || minusExpressionPending() || parenthesizedExpressionPending();
    }

    private boolean incrementPending() {
        return check(IDENTIFIER) && checkNext(INCREMENT);
    }

    private boolean decrementPending() {
        return check(IDENTIFIER) && checkNext(DECREMENT);
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

    private boolean negationExpressionPending() {
        return check(EXCLAMATION);
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