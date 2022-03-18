package com.sigma.recognizing;

import com.sigma.Sigma;
import com.sigma.lexicalAnalysis.Lexeme;
import com.sigma.lexicalAnalysis.TokenType;

import java.util.ArrayList;

import static com.sigma.lexicalAnalysis.TokenType.*;

public class Recognizer {
    private static final boolean printDebugMessages = false;
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

    private Lexeme consume(TokenType expected) {
        if (check(expected)) {
            if (printDebugMessages) log(expected.toString());
            return advance();
        } else error("Expected " + expected + " but found " + currentLexeme + ".");
        return null;
    }

    private Lexeme advance() {
        currentLexeme = lexemes.get(nextLexemeIndex);
        nextLexemeIndex++;
        return currentLexeme;
    }

    // Consumption functions
    public Lexeme program() {
        if (printDebugMessages) log("program");
        Lexeme program = new Lexeme(PROGRAM, currentLexeme.getLineNumber());
        if (statementListPending()) program.setLeft(statementList());
        return program;
    }

    private Lexeme statementList() {
        if (printDebugMessages) log("statementList");
        Lexeme statementList = new Lexeme(STATEMENT_LIST, currentLexeme.getLineNumber());
        statementList.setLeft(statement());
        if (statementPending()) statementList.setRight(statementList());
        return statementList;
    }

    private Lexeme statement() {
        Lexeme statement = null;
        if (printDebugMessages) log("statement");
        if (variableDeclarationPending()) {
            statement = variableDeclaration();
            consume(BANGBANG);
        } else if (assignmentPending()) {
            statement = assignment();
            consume(BANGBANG);
        } else if (functionDefinitionPending()) statement = functionDefinition();
        else if (loopPending()) statement = loop();
        else if (ifStatementPending()) statement = ifStatement();
        else if (commentPending()) statement = comment();
        else if (expressionPending()) {
            statement = expression();
            consume(BANGBANG);
        } else error("Expected statement.");
        return statement;
    }

    private Lexeme variableDeclaration() {
        if (printDebugMessages) log("variableDeclaration");
        consume(VAR_KEYWORD);
        Lexeme declaration = new Lexeme(VARIABLE_DECLARATION, currentLexeme.getLineNumber());
        declaration.setLeft(consume(IDENTIFIER));
        if (check(ASSIGN_OPERATOR)) {
            consume(ASSIGN_OPERATOR);
            declaration.setRight(expression());
        }
        return declaration;
    }

    private Lexeme assignment() {
        if (printDebugMessages) log("assignment");
        Lexeme assignment = new Lexeme(ASSIGNMENT, currentLexeme.getLineNumber());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        if (unaryOperatorPending()) {
            glue.setLeft(unaryOperator());
            glue.setRight(consume(IDENTIFIER));
            assignment.setRight(glue);
        } else if (check(IDENTIFIER)) {
            glue.setLeft(consume(IDENTIFIER));
            glue.setRight(regularAssignment());
            assignment.setLeft(glue);
        } else error("Expected assignment operator.");
        return assignment;
    }

    private Lexeme functionDefinition() {
        if (printDebugMessages) log("functionDefinition");
        Lexeme funcDef = new Lexeme(FUNCTION_DEFINITION, currentLexeme.getLineNumber());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        consume(FUNC_KEYWORD);
        glue.setLeft(consume(IDENTIFIER));
        consume(ASSIGN_OPERATOR);
        glue.setRight(functionArgs());
        funcDef.setLeft(glue);
        funcDef.setRight(block());
        consume(BANGBANG);
        return funcDef;
    }

    private Lexeme loop() {
        if (printDebugMessages) log("loop");
        if (forLoopPending()) return forLoop();
        else if (foreachLoopPending()) return foreachLoop();
        else if (whenLoopPending()) return whenLoop();
        else if (loopLoopPending()) return loopLoop();
        else error("Expected loop.");
        return null;
    }

    private Lexeme ifStatement() {
        if (printDebugMessages) log("ifStatement");
        Lexeme ifStatement = new Lexeme(IF_STATEMENT, currentLexeme.getLineNumber());
        Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        consume(IF_KEYWORD);
        glue1.setLeft(parenthesizedExpression());
        glue2.setRight(block());
        if (butifStatementPending()) {
            glue2.setLeft(butifStatementList());
        }
        if (butStatementPending()) glue2.setRight(butStatement());
        ifStatement.setLeft(glue1);
        ifStatement.setRight(glue2);
        return ifStatement;
    }

    private Lexeme comment() {
        if (printDebugMessages) log("comment");
        return consume(COMMENT);
    }

    private Lexeme expression() {
        if (printDebugMessages) log("expression");
        if (binaryExpressionPending()) return binaryExpression();
        else if (primaryPending()) return primary();
        else error("Expression expected.");
        return null;
    }

    private Lexeme type() {
        if (printDebugMessages) log("type");
        if (check(STR_KEYWORD)) return consume(STR_KEYWORD);
        else if (check(NUM_KEYWORD)) return consume(NUM_KEYWORD);
        else if (check(TF_KEYWORD)) return consume(TF_KEYWORD);
        else if (check(ARR_KEYWORD)) return consume(ARR_KEYWORD);
        else error("Expected type keyword.");
        return null;
    }

    private Lexeme regularAssignment() {
        if (printDebugMessages) log("regularAssignment");
        Lexeme regularAssignment = new Lexeme(REGULAR_ASSIGNMENT, currentLexeme.getLineNumber());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        if (check(ASSIGN_OPERATOR)) glue.setLeft(consume(ASSIGN_OPERATOR));
        else if (operatorAssignmentPending()) glue.setRight(operatorAssignment());
        else error("Expected assignment operator.");
        regularAssignment.setLeft(glue);
        regularAssignment.setRight(expression());
        return regularAssignment;
    }

    private Lexeme unaryOperator() {
        if (printDebugMessages) log("unaryOperator");
        if (check(INCREMENT)) return consume(INCREMENT);
        else if (check(DECREMENT)) return consume(DECREMENT);
        else if (check(NOT_KEYWORD)) return consume(NOT_KEYWORD);
        else if (check(EXCLAMATION)) return consume(EXCLAMATION);
        else error("Expected unary assignment operator.");
        return null;
    }

    private Lexeme functionArgs() {
        if (printDebugMessages) log("functionArgs");
        Lexeme functionArgs = new Lexeme(FUNCTION_ARGS, currentLexeme.getLineNumber());
        if (functionArgPending()) {
            functionArgs.setLeft(functionArg());
        }
        if (functionArgPending()) {
            functionArgs.setRight(functionArgs());
        }
        return functionArgs;
    }

    private Lexeme block() {
        if (printDebugMessages) log("block");
        consume(DOUBLE_FORWARD);
        if (statementListPending()) {
            return statementList();
        }
        consume(DOUBLE_BACKWARD);
        return null;
    }

    private Lexeme forLoop() {
        if (printDebugMessages) log("forLoop");
        Lexeme forLoop = new Lexeme(FOR_LOOP, currentLexeme.getLineNumber());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        consume(FOR_KEYWORD);
        consume(OPEN_CURLY);
        glue2.setLeft(variableDeclaration());
        consume(BANGBANG);
        glue2.setRight(expression());
        consume(BANGBANG);
        glue.setRight(assignment());
        consume(CLOSED_CURLY);
        forLoop.setRight(block());
        glue.setLeft(glue2);
        forLoop.setLeft(glue);
        return forLoop;
    }

    private Lexeme foreachLoop() {
        if (printDebugMessages) log("foreachLoop");
        Lexeme foreachLoop = new Lexeme(FOREACH_LOOP, currentLexeme.getLineNumber());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        consume(FOREACH_KEYWORD);
        consume(OPEN_CURLY);
        consume(VAR_KEYWORD);
        glue.setLeft(consume(IDENTIFIER));
        consume(OF_KEYWORD);
        glue.setRight(consume(IDENTIFIER));
        consume(CLOSED_CURLY);
        foreachLoop.setLeft(glue);
        foreachLoop.setRight(block());
        return foreachLoop;
    }

    private Lexeme whenLoop() {
        if (printDebugMessages) log("whenLoop");
        Lexeme whenLoop = new Lexeme(WHEN_LOOP, currentLexeme.getLineNumber());
        consume(WHEN_KEYWORD);
        whenLoop.setLeft(parenthesizedExpression());
        whenLoop.setRight(block());
        return whenLoop;
    }

    private Lexeme loopLoop() {
        if (printDebugMessages) log("loopLoop");
        Lexeme loopLoop = new Lexeme(LOOP_LOOP, currentLexeme.getLineNumber());
        consume(LOOP_KEYWORD);
        consume(OPEN_CURLY);
        loopLoop.setLeft(consume(NUMBER));
        consume(CLOSED_CURLY);
        loopLoop.setRight(block());
        return loopLoop;
    }

    private Lexeme butifStatementList() {
        if (printDebugMessages) log("butifStatementList");
        Lexeme butifStatementList = new Lexeme(BUTIF_STATEMENT_LIST, currentLexeme.getLineNumber());
        if (butifStatementPending()) {
            butifStatementList.setRight(butifStatement());
        }
        if (butifStatementPending()) {
            butifStatementList.setLeft(butifStatementList());
        }
        return butifStatementList;
    }

    private Lexeme butifStatement() {
        if (printDebugMessages) log("butifStatement");
        Lexeme butIfStatement = new Lexeme(BUTIF_STATEMENT, currentLexeme.getLineNumber());
        consume(BUTIF_KEYWORD);
        butIfStatement.setLeft(parenthesizedExpression());
        butIfStatement.setRight(block());
        return butIfStatement;
    }

    private Lexeme butStatement() {
        if (printDebugMessages) log("butStatement");
        Lexeme butStatement = new Lexeme(BUT_STATEMENT, currentLexeme.getLineNumber());
        consume(BUT_KEYWORD);
        butStatement.setLeft(block());
        return butStatement;
    }

    private Lexeme binaryExpression() {
        if (printDebugMessages) log("binaryExpression");
        Lexeme binaryExpression = new Lexeme(BINARY_EXPRESSION, currentLexeme.getLineNumber());
        Lexeme firstOperand = primary();
        Lexeme binaryOperator = binaryOperator();
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        if (binaryExpressionPending()) glue.setRight(binaryExpression());
        else if (primaryPending()) glue.setLeft(primary());
        else error("Expected primary or further expression.");
        binaryOperator.setLeft(firstOperand);
        binaryOperator.setRight(glue);
        binaryExpression.setLeft(binaryOperator);
        return binaryExpression;
    }

    private Lexeme primary() {
        if (printDebugMessages) log("primary");
        if (unaryExpressionPending()) return unaryExpression();
        else if (check(NUMBER)) return consume(NUMBER);
        else if (functionCallPending()) return functionCall();
        else if (castPending()) return cast();
        else if (check(IDENTIFIER)) return consume(IDENTIFIER);
        else if (check(STRING)) return consume(STRING);
        else if (booleanPending()) return bool();
        else if (arrayPending()) return array();
        else error("Expected primary.");
        return null;
    }

    private Lexeme unaryExpression() {
        if (printDebugMessages) log("unaryExpression");
        Lexeme unaryExpression = new Lexeme(UNARY_EXPRESSION, currentLexeme.getLineNumber());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        if (unaryOperatorPending()) {
            glue.setLeft(unaryOperator());
            glue.setRight(primary());
            unaryExpression.setLeft(glue);
        } else if (minusExpressionPending()) {
            glue.setLeft(minusExpression());
            unaryExpression.setRight(glue);
        } else if (parenthesizedExpressionPending()) {
            glue.setRight(parenthesizedExpression());
            unaryExpression.setRight(glue);
        } else error("Expected unary expression.");
        return unaryExpression;
    }

    private Lexeme operatorAssignment() {
        if (printDebugMessages) log("operatorAssignment");
        if (check(PLUS_ASSIGNMENT)) return consume(PLUS_ASSIGNMENT);
        else if (check(MINUS_ASSIGNMENT)) return consume(MINUS_ASSIGNMENT);
        else if (check(DIVIDE_ASSIGNMENT)) return consume(DIVIDE_ASSIGNMENT);
        else if (check(TIMES_ASSIGNMENT)) return consume(TIMES_ASSIGNMENT);
        else if (check(DOUBLE_DIVIDE_ASSIGNMENT)) return consume(DOUBLE_DIVIDE_ASSIGNMENT);
        else if (check(CARET_ASSIGNMENT)) return consume(CARET_ASSIGNMENT);
        else if (check(PERCENT_ASSIGNMENT)) return consume(PERCENT_ASSIGNMENT);
        else error("Expected assignment operator.");
        return null;
    }

    private Lexeme functionArg() {
        if (printDebugMessages) log("functionArg");
        consume(VAR_KEYWORD);
        return consume(IDENTIFIER);
    }

    private Lexeme binaryOperator() {
        if (printDebugMessages) log("binaryOperator");
        if (binaryArithmeticOperatorPending()) return binaryArithmeticOperator();
        else if (binaryComparatorPending()) return binaryComparator();
        else if (binaryBooleanOperatorPending()) return binaryBooleanOperator();
        else error("Expected binary operator.");
        return null;
    }

    private Lexeme bool() {
        if (printDebugMessages) log("boolean");
        if (check(TRUE_KEYWORD)) return consume(TRUE_KEYWORD);
        else if (check(FALS_KEYWORD)) return consume(FALS_KEYWORD);
        else error("Expected boolean.");
        return null;
    }

    private Lexeme array() {
        if (printDebugMessages) log("array");
        consume(OPEN_PAREN);
        Lexeme arrayElements = arrayElements();
        consume(CLOSED_PAREN);
        return arrayElements;
    }

    private Lexeme cast() {
        if (printDebugMessages) log("cast");
        Lexeme cast = new Lexeme(CAST, currentLexeme.getLineNumber());
        cast.setLeft(consume(IDENTIFIER));
        consume(PERIOD);
        cast.setRight(type());
        return cast;
    }

    private Lexeme functionCall() {
        if (printDebugMessages) log("functionCall");
        Lexeme functionCall = new Lexeme(FUNCTION_CALL, currentLexeme.getLineNumber());
        functionCall.setLeft(consume(IDENTIFIER));
        consume(OPEN_CURLY);
        functionCall.setRight(callArguments());
        consume(CLOSED_CURLY);
        return functionCall;
    }

    private Lexeme parenthesizedExpression() {
        if (printDebugMessages) log("parenthesizedExpression");
        consume(OPEN_CURLY);
        Lexeme expression = expression();
        consume(CLOSED_CURLY);
        return expression;
    }

    private Lexeme binaryArithmeticOperator() {
        if (printDebugMessages) log("binaryArithmeticOperator");
        if (check(PLUS)) return consume(PLUS);
        else if (check(MINUS)) return consume(MINUS);
        else if (check(DIVIDE)) return consume(DIVIDE);
        else if (check(TIMES)) return consume(TIMES);
        else if (check(DOUBLE_DIVIDE)) return consume(DOUBLE_DIVIDE);
        else if (check(CARET)) return consume(CARET);
        else if (check(PERCENT)) return consume(PERCENT);
        else error("Expected binary arithmetic operator.");
        return null;
    }

    private Lexeme binaryComparator() {
        if (printDebugMessages) log("binaryComparator");
        if (check(GREATER)) return consume(GREATER);
        else if (check(LESS)) return consume(LESS);
        else if (check(GREATER_QUESTION)) return consume(GREATER_QUESTION);
        else if (check(LESS_QUESTION)) return consume(LESS_QUESTION);
        else if (check(GEQ)) return consume(GEQ);
        else if (check(LEQ)) return consume(LEQ);
        else if (check(QUESTION)) return consume(QUESTION);
        else if (check(NOT_QUESTION)) return consume(NOT_QUESTION);
        else if (check(DOUBLE_QUESTION)) return consume(DOUBLE_QUESTION);
        else if (check(NOT_DOUBLE_QUESTION)) return consume(NOT_DOUBLE_QUESTION);
        else if (check(APPROX)) return consume(APPROX);
        else if (check(NOT_APPROX)) return consume(NOT_APPROX);
        else error("Expected binary comparator.");
        return null;
    }

    private Lexeme binaryBooleanOperator() {
        if (printDebugMessages) log("binaryBooleanOperator");
        if (check(AND_KEYWORD)) return consume(AND_KEYWORD);
        else if (check(OR_KEYWORD)) return consume(OR_KEYWORD);
        else if (check(NAND_KEYWORD)) return consume(NAND_KEYWORD);
        else if (check(NOR_KEYWORD)) return consume(NOR_KEYWORD);
        else if (check(XOR_KEYWORD)) return consume(XOR_KEYWORD);
        else if (check(XNOR_KEYWORD)) return consume(XNOR_KEYWORD);
        else if (check(IMPLIES_KEYWORD)) return consume(IMPLIES_KEYWORD);
        else error("Expected boolean operator.");
        return null;
    }

    private Lexeme arrayElements() {
        if (printDebugMessages) log("arrayElements");
        Lexeme arrayElements = new Lexeme(ARRAY_ELEMENTS, currentLexeme.getLineNumber());
        if (primaryPending()) {
            arrayElements.setLeft(primary());
        }
        if (primaryPending()) {
            arrayElements.setRight(arrayElements());
        }
        return arrayElements;
    }

    private Lexeme callArguments() {
        if (printDebugMessages) log("callArguments");
        Lexeme callArguments = new Lexeme(CALL_ARGUMENTS, currentLexeme.getLineNumber());
        if (primaryPending()) {
            callArguments.setLeft(primary());
        }
        if (primaryPending()) {
            callArguments.setRight(callArguments());
        }
        return callArguments;
    }

    private Lexeme minusExpression() {
        if (printDebugMessages) log("minusExpression");
        Lexeme minusExpression = new Lexeme(MINUS_EXPRESSION, currentLexeme.getLineNumber());
        minusExpression.setLeft(consume(MINUS));
        minusExpression.setRight(expression());
        return minusExpression;
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

    private boolean regularAssignmentPendingNext() {
        return checkNext(ASSIGN_OPERATOR) || operatorAssignmentPendingNext();
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

    private boolean functionArgPending() {
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