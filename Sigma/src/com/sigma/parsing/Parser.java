package com.sigma.parsing;

import com.sigma.Sigma;
import com.sigma.lexicalAnalysis.Lexeme;
import com.sigma.lexicalAnalysis.TokenType;

import java.util.ArrayList;

import static com.sigma.lexicalAnalysis.TokenType.*;


// TODO something like {not a.num} ? 0 or a.num ? 0 does not work, doesn't recognize it as a binary expression
// TODO fix that weird binary expression yellow thing
// TODO order of operations

public class Parser {
    private static final boolean printDebugMessages = false;
    private final ArrayList<Lexeme> lexemes;
    private Lexeme currentLexeme;
    private int nextLexemeIndex;

    // Constructor
    public Parser(ArrayList<Lexeme> lexemes) {
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
        Lexeme returnLexeme = null;
        if (check(expected)) {
            if (printDebugMessages) log(expected.toString());
            returnLexeme = currentLexeme;
            advance();
        } else error("Expected " + expected + " but found " + currentLexeme + ".");
        return returnLexeme;
    }

    private void advance() {
        currentLexeme = lexemes.get(nextLexemeIndex);
        nextLexemeIndex++;
    }

    // Consumption functions
    public Lexeme program() {
        if (printDebugMessages) log("program");
        Lexeme program = new Lexeme(PROGRAM, currentLexeme.getLineNumber());
        if (statementListPending()) program.addChild(statementList());
        return program;
    }

    private Lexeme statementList() {
        if (printDebugMessages) log("statementList");
        Lexeme statementList = new Lexeme(STATEMENT_LIST, currentLexeme.getLineNumber());
        while (statementPending()) {
            statementList.addChild(statement());
        }
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
        else if (changeStatementPending()) statement = changeStatement();
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
        declaration.addChild(consume(IDENTIFIER));
        if (check(ASSIGN_OPERATOR)) {
            consume(ASSIGN_OPERATOR);
            declaration.addChild(expression());
        }
        return declaration;
    }

    private Lexeme assignment() {
        if (printDebugMessages) log("assignment");
        Lexeme assignment = new Lexeme(ASSIGNMENT, currentLexeme.getLineNumber());
        if (unaryOperatorPending()) {
            assignment.addChild(unaryOperator());
            assignment.addChild(consume(IDENTIFIER));
        } else if (check(IDENTIFIER)) {
            assignment.addChild(consume(IDENTIFIER));
            assignment.addChild(regularAssignment());
        } else error("Expected assignment operator.");
        return assignment;
    }

    private Lexeme functionDefinition() {
        if (printDebugMessages) log("functionDefinition");
        Lexeme funcDef = new Lexeme(FUNCTION_DEFINITION, currentLexeme.getLineNumber());
        consume(FUNC_KEYWORD);
        funcDef.addChild(consume(IDENTIFIER));
        consume(ASSIGN_OPERATOR);
        funcDef.addChild(functionArgs());
        if (check(OPEN_SQUARE)) {
            consume(OPEN_SQUARE);
            funcDef.addChild(optionalFunctionArgs());
            consume(CLOSED_SQUARE);
        }
        funcDef.addChild(block());
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
        consume(IF_KEYWORD);
        ifStatement.addChild(parenthesizedExpression());
        ifStatement.addChild(block());
        if (butifStatementPending()) {
            ifStatement.addChild(butifStatementList());
        }
        if (butStatementPending()) ifStatement.addChild(butStatement());
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
        if (check(ASSIGN_OPERATOR)) regularAssignment.addChild(consume(ASSIGN_OPERATOR));
        else if (operatorAssignmentPending()) regularAssignment.addChild(operatorAssignment());
        else error("Expected assignment operator.");
        regularAssignment.addChild(expression());
        return regularAssignment;
    }

    private Lexeme unaryOperator() {
        if (printDebugMessages) log("unaryOperator");
        if (check(INCREMENT)) return consume(INCREMENT);
        else if (check(DECREMENT)) return consume(DECREMENT);
        else if (check(NOT_KEYWORD)) return consume(NOT_KEYWORD);
        else if (check(EXCLAMATION)) return consume(EXCLAMATION);
        else if (check(MINUS)) return consume(MINUS);
        else error("Expected unary assignment operator.");
        return null;
    }

    private Lexeme functionArgs() {
        if (printDebugMessages) log("functionArgs");
        Lexeme functionArgs = new Lexeme(FUNCTION_ARGS, currentLexeme.getLineNumber());
        while (functionArgPending()) {
            functionArgs.addChild(functionArg());
        }
        return functionArgs;
    }

    private Lexeme optionalFunctionArgs() {
        if (printDebugMessages) log("optionalFunctionArgs");
        Lexeme optionalFunctionArgs = new Lexeme(OPTIONAL_FUNCTION_ARGS, currentLexeme.getLineNumber());
        optionalFunctionArgs.addChild(functionArg());
        while (functionArgPending()) {
            optionalFunctionArgs.addChild(functionArg());
        }
        return optionalFunctionArgs;
    }

    private Lexeme block() {
        if (printDebugMessages) log("block");
        Lexeme block = null;
        consume(DOUBLE_FORWARD);
        if (statementPending()) {
            block = statementList();
        }
        consume(DOUBLE_BACKWARD);
        return block;
    }

    private Lexeme forLoop() {
        if (printDebugMessages) log("forLoop");
        Lexeme forLoop = new Lexeme(FOR_LOOP, currentLexeme.getLineNumber());
        consume(FOR_KEYWORD);
        consume(OPEN_CURLY);
        forLoop.addChild(variableDeclaration());
        consume(BANGBANG);
        forLoop.addChild(expression());
        consume(BANGBANG);
        forLoop.addChild(assignment());
        consume(CLOSED_CURLY);
        forLoop.addChild(block());
        return forLoop;
    }

    private Lexeme foreachLoop() {
        if (printDebugMessages) log("foreachLoop");
        Lexeme foreachLoop = new Lexeme(FOREACH_LOOP, currentLexeme.getLineNumber());
        consume(FOREACH_KEYWORD);
        consume(OPEN_CURLY);
        consume(VAR_KEYWORD);
        foreachLoop.addChild(consume(IDENTIFIER));
        consume(OF_KEYWORD);
        foreachLoop.addChild(consume(IDENTIFIER));
        consume(CLOSED_CURLY);
        foreachLoop.addChild(block());
        return foreachLoop;
    }

    private Lexeme whenLoop() {
        if (printDebugMessages) log("whenLoop");
        Lexeme whenLoop = new Lexeme(WHEN_LOOP, currentLexeme.getLineNumber());
        consume(WHEN_KEYWORD);
        consume(OPEN_CURLY);
        whenLoop.addChild(expression());
        consume(CLOSED_CURLY);
        whenLoop.addChild(block());
        return whenLoop;
    }

    private Lexeme loopLoop() {
        if (printDebugMessages) log("loopLoop");
        Lexeme loopLoop = new Lexeme(LOOP_LOOP, currentLexeme.getLineNumber());
        consume(LOOP_KEYWORD);
        consume(OPEN_CURLY);
        loopLoop.addChild(consume(NUMBER));
        consume(CLOSED_CURLY);
        loopLoop.addChild(block());
        return loopLoop;
    }

    private Lexeme butifStatementList() {
        if (printDebugMessages) log("butifStatementList");
        Lexeme butifStatementList = new Lexeme(BUTIF_STATEMENT_LIST, currentLexeme.getLineNumber());
        while (butifStatementPending()) {
            butifStatementList.addChild(butifStatement());
        }
        return butifStatementList;
    }

    private Lexeme butifStatement() {
        if (printDebugMessages) log("butifStatement");
        Lexeme butIfStatement = new Lexeme(BUTIF_STATEMENT, currentLexeme.getLineNumber());
        consume(BUTIF_KEYWORD);
        butIfStatement.addChild(parenthesizedExpression());
        butIfStatement.addChild(block());
        return butIfStatement;
    }

    private Lexeme butStatement() {
        if (printDebugMessages) log("butStatement");
        Lexeme butStatement = new Lexeme(BUT_STATEMENT, currentLexeme.getLineNumber());
        consume(BUT_KEYWORD);
        butStatement.addChild(block());
        return butStatement;
    }

    private Lexeme changeStatement() {
        if (printDebugMessages) log("changeStatement");
        Lexeme changeStatement = new Lexeme(CHANGE_STATEMENT, currentLexeme.getLineNumber());
        consume(CHANGE_KEYWORD);
        consume(OPEN_CURLY);
        changeStatement.addChild(consume(IDENTIFIER));
        consume(CLOSED_CURLY);
        consume(DOUBLE_FORWARD);
        changeStatement.addChild(changeCases());
        consume(DOUBLE_BACKWARD);
        consume(BANGBANG);
        return changeStatement;
    }

    private Lexeme changeCases() {
        if (printDebugMessages) log("cases");
        Lexeme changeCases = new Lexeme(CHANGE_CASES, currentLexeme.getLineNumber());
        changeCases.addChild(changeCase());
        if (changeCasePending()) {
            changeCases.addChild(changeCase());
        }
        return changeCases;
    }

    private Lexeme changeCase() {
        if (printDebugMessages) log("case");
        Lexeme changeCase = new Lexeme(CHANGE_CASE, currentLexeme.getLineNumber());
        consume(CASE_KEYWORD);
        consume(OPEN_CURLY);
        changeCase.addChild(expression());
        consume(CLOSED_CURLY);
        changeCase.addChild(block());
        return changeCase;
    }

    private Lexeme binaryExpression() {
        if (printDebugMessages) log("binaryExpression");
        Lexeme binaryExpression = new Lexeme(BINARY_EXPRESSION, currentLexeme.getLineNumber());
        Lexeme firstOperand = primary();
        Lexeme binaryOperator = binaryOperator();
        binaryOperator.addChild(firstOperand);
        if (binaryExpressionPending()) binaryOperator.addChild(binaryExpression());
        else if (primaryPending()) binaryOperator.addChild(primary());
        else error("Expected primary or further expression.");
        binaryExpression.addChild(binaryOperator);
        return binaryExpression;
    }

    private Lexeme primary() { // TODO add binary expression case
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
        if (unaryOperatorPending()) {
            unaryExpression.addChild(unaryOperator());
            unaryExpression.addChild(primary());
        } else if (parenthesizedExpressionPending()) {
            unaryExpression.addChild(parenthesizedExpression());
        } else error("Expected unary expression.");
        return unaryExpression;
    }

    private Lexeme operatorAssignment() { // TODO clean up
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
        cast.addChild(consume(IDENTIFIER));
        consume(PERIOD);
        cast.addChild(type());
        return cast;
    }

    private Lexeme functionCall() {
        if (printDebugMessages) log("functionCall");
        Lexeme functionCall = new Lexeme(FUNCTION_CALL, currentLexeme.getLineNumber());
        functionCall.addChild(consume(IDENTIFIER));
        consume(OPEN_CURLY);
        functionCall.addChild(callArguments());
        consume(CLOSED_CURLY);
        return functionCall;
    }

    private Lexeme parenthesizedExpression() {
        if (printDebugMessages) log("parenthesizedExpression");
        consume(OPEN_CURLY);
        Lexeme expr = expression();
        consume(CLOSED_CURLY);
        return expr;
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
        while (primaryPending()) {
            arrayElements.addChild(primary());
        }
        return arrayElements;
    }

    private Lexeme callArguments() {
        if (printDebugMessages) log("callArguments");
        Lexeme callArguments = new Lexeme(CALL_ARGUMENTS, currentLexeme.getLineNumber());
        while (primaryPending()) {
            callArguments.addChild(primary());
        }
        return callArguments;
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
                || changeStatementPending()
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

    private boolean changeStatementPending() {
        return check(CHANGE_KEYWORD);
    }

    private boolean changeCasePending() {
        return check(CASE_KEYWORD);
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
        return check(INCREMENT) || check(DECREMENT) || check(NOT_KEYWORD) || check(EXCLAMATION) || check(MINUS);
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
        return unaryOperatorPending() || parenthesizedExpressionPending();
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