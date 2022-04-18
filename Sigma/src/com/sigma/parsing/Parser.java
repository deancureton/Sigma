package com.sigma.parsing;

import com.sigma.Sigma;
import com.sigma.lexicalAnalysis.Lexeme;
import com.sigma.lexicalAnalysis.TokenType;

import java.util.ArrayList;

import static com.sigma.lexicalAnalysis.TokenType.*;

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
            log(expected.toString());
            returnLexeme = currentLexeme;
            advance();
        } else error("Expected " + expected + " but found " + currentLexeme + ".");
        return returnLexeme;
    }

    private Lexeme consume() {
        log(currentLexeme.getType().toString());
        Lexeme returnLexeme = currentLexeme;
        advance();
        return returnLexeme;
    }

    private void advance() {
        currentLexeme = lexemes.get(nextLexemeIndex);
        nextLexemeIndex++;
    }

    // Consumption functions
    public Lexeme program() {
        log("program");
        Lexeme program = new Lexeme(PROGRAM, currentLexeme.getLineNumber());
        if (statementListPending()) program.addChild(statementList());
        return program;
    }

    private Lexeme statementList() {
        log("statementList");
        Lexeme statementList = new Lexeme(STATEMENT_LIST, currentLexeme.getLineNumber());
        while (statementPending()) {
            statementList.addChild(statement());
        }
        return statementList;
    }

    private Lexeme statement() {
        Lexeme statement = null;
        log("statement");
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
        log("variableDeclaration");
        consume(VAR_KEYWORD);
        Lexeme declaration = new Lexeme(VARIABLE_DECLARATION, currentLexeme.getLineNumber());
        declaration.addChild(consume(IDENTIFIER));
        consume(ASSIGN_OPERATOR);
        declaration.addChild(expression());
        return declaration;
    }

    private Lexeme assignment() {
        log("assignment");
        Lexeme assignment = new Lexeme(ASSIGNMENT, currentLexeme.getLineNumber());
        if (check(INCREMENT)) {
            Lexeme op = consume(INCREMENT);
            assignment.addChild(consume(IDENTIFIER));
            assignment.addChild(op);
        } else if (check(DECREMENT)) {
            Lexeme op = consume(DECREMENT);
            assignment.addChild(consume(IDENTIFIER));
            assignment.addChild(op);
        } else if (check(IDENTIFIER)) {
            assignment.addChild(consume(IDENTIFIER));
            assignment.addChild(regularAssignment());
        } else error("Expected assignment operator.");
        return assignment;
    }

    private Lexeme functionDefinition() {
        log("functionDefinition");
        Lexeme funcDef = new Lexeme(FUNCTION_DEFINITION, currentLexeme.getLineNumber());
        consume(FUNC_KEYWORD);
        funcDef.addChild(consume(IDENTIFIER));
        consume(ASSIGN_OPERATOR);
        funcDef.addChild(functionArgs());
        funcDef.addChild(block());
        consume(BANGBANG);
        return funcDef;
    }

    private Lexeme loop() {
        log("loop");
        if (forLoopPending()) return forLoop();
        else if (foreachLoopPending()) return foreachLoop();
        else if (whenLoopPending()) return whenLoop();
        else if (loopLoopPending()) return loopLoop();
        else error("Expected loop.");
        return null;
    }

    private Lexeme ifStatement() {
        log("ifStatement");
        Lexeme ifStatement = new Lexeme(IF_STATEMENT, currentLexeme.getLineNumber());
        consume(IF_KEYWORD);
        consume(OPEN_CURLY);
        ifStatement.addChild(expression());
        consume(CLOSED_CURLY);

        consume(DOUBLE_FORWARD);
        if (statementPending()) {
            ifStatement.addChild(statementList());
        } else {
            ifStatement.addChild(new Lexeme(STATEMENT_LIST, currentLexeme.getLineNumber()));
        }
        consume(DOUBLE_BACKWARD);

        ifStatement.addChild(butifStatementList());
        if (butStatementPending()) ifStatement.addChild(butStatement());
        return ifStatement;
    }

    private Lexeme comment() {
        log("comment");
        return consume(COMMENT);
    }

    private Lexeme expression() {
        log("expression");
        Lexeme expression = new Lexeme(EXPRESSION, currentLexeme.getLineNumber());
        expression.addChild(orExpr());
        return expression;
    }

    private Lexeme orExpr() {
        log("orExpr");
        Lexeme xorExpr = xorExpr();
        if (check(OR_KEYWORD) || check(NOR_KEYWORD)) {
            Lexeme orOperator = consume();
            orOperator.addChild(xorExpr);
            orOperator.addChild(orExpr());
            return orOperator;
        }
        return xorExpr;
    }

    private Lexeme xorExpr() {
        log("xorExpr");
        Lexeme andExpr = andExpr();
        if (check(XOR_KEYWORD) || check(XNOR_KEYWORD)) {
            Lexeme xorOperator = consume();
            xorOperator.addChild(andExpr);
            xorOperator.addChild(xorExpr());
            return xorOperator;
        }
        return andExpr;
    }

    private Lexeme andExpr() {
        log("andExpr");
        Lexeme weakEqualityExpr = weakEqualityExpr();
        if (check(AND_KEYWORD) || check(NAND_KEYWORD)) {
            Lexeme andOperator = consume();
            andOperator.addChild(weakEqualityExpr);
            andOperator.addChild(andExpr());
            return andOperator;
        }
        return weakEqualityExpr;
    }

    private Lexeme weakEqualityExpr() {
        log("weakEqualityExpr");
        Lexeme equalityExpr = equalityExpr();
        if (check(DOUBLE_QUESTION) || check(NOT_DOUBLE_QUESTION) || check(APPROX) || check(NOT_APPROX)) {
            Lexeme weakEqualityOperator = consume();
            weakEqualityOperator.addChild(equalityExpr);
            weakEqualityOperator.addChild(weakEqualityExpr());
            return weakEqualityOperator;
        }
        return equalityExpr;
    }

    private Lexeme equalityExpr() {
        log("equalityExpr");
        Lexeme comparisonExpr = comparisonExpr();
        if (check(QUESTION) || check(NOT_QUESTION)) {
            Lexeme equalityOperator = consume();
            equalityOperator.addChild(comparisonExpr);
            equalityOperator.addChild(equalityExpr());
            return equalityOperator;
        }
        return comparisonExpr;
    }

    private Lexeme comparisonExpr() {
        log("comparisonExpr");
        Lexeme sumExpr = sumExpr();
        if (check(LESS) || check(GREATER) || check(LESS_QUESTION) || check(GREATER_QUESTION) || check(GEQ) || check(LEQ)) {
            Lexeme comparisonOperator = consume();
            comparisonOperator.addChild(sumExpr);
            comparisonOperator.addChild(comparisonExpr());
            return comparisonOperator;
        }
        return sumExpr;
    }

    private Lexeme sumExpr() {
        log("sumExpr");
        Lexeme productExpr = productExpr();
        if (check(PLUS) || check(MINUS)) {
            Lexeme sumOperator = consume();
            sumOperator.addChild(productExpr);
            sumOperator.addChild(sumExpr());
            return sumOperator;
        }
        return productExpr;
    }

    private Lexeme productExpr() {
        log("productExpr");
        Lexeme exponentExpr = exponentExpr();
        if (check(TIMES) || check(DOUBLE_DIVIDE) || check(DIVIDE) || check(PERCENT)) {
            Lexeme productOperator = consume();
            productOperator.addChild(exponentExpr);
            productOperator.addChild(productExpr());
            return productOperator;
        }
        return exponentExpr;
    }

    private Lexeme exponentExpr() {
        log("exponentExpr");
        Lexeme unaryExpr = unaryExpr();
        if (check(CARET)) {
            Lexeme exponentOperator = consume(CARET);
            exponentOperator.addChild(unaryExpr);
            exponentOperator.addChild(exponentExpr());
            return exponentOperator;
        }
        return unaryExpr;
    }

    private Lexeme unaryExpr() {
        log("unaryExpr");
        if (check(EXCLAMATION) || check(NOT_KEYWORD) || check(MINUS) || check(INCREMENT) || check(DECREMENT)) {
            Lexeme unaryOperator = consume();
            unaryOperator.addChild(unaryExpr());
            return unaryOperator;
        } else if (primaryPending()) {
            return primary();
        } else {
            error("Expected unary expression.");
            return null;
        }
    }

    private Lexeme primary() {
        log("primary");
        if (functionCallPending()) return functionCall();
        else if (castPending()) return cast();
        else if (check(NUMBER)) return consume(NUMBER);
        else if (check(IDENTIFIER)) return consume(IDENTIFIER);
        else if (check(STRING)) return consume(STRING);
        else if (check(BOOLEAN)) return consume(BOOLEAN);
        else if (check(NOTHING_KEYWORD)) return consume(NOTHING_KEYWORD);
        else if (arrayPending()) return array();
        else if (parenthesizedExpressionPending()) return parenthesizedExpression();
        else error("Expected primary.");
        return null;
    }

    private Lexeme parenthesizedExpression() {
        log("parenthesizedExpression");
        Lexeme parenthesizedExpression;
        consume(OPEN_CURLY);
        parenthesizedExpression = expression();
        consume(CLOSED_CURLY);
        return parenthesizedExpression;
    }

    private Lexeme type() {
        log("type");
        if (check(STR_KEYWORD)) return consume(STR_KEYWORD);
        else if (check(NUM_KEYWORD)) return consume(NUM_KEYWORD);
        else if (check(TF_KEYWORD)) return consume(TF_KEYWORD);
        else if (check(ARR_KEYWORD)) return consume(ARR_KEYWORD);
        else error("Expected type keyword.");
        return null;
    }

    private Lexeme regularAssignment() {
        log("regularAssignment");
        Lexeme regularAssignment = new Lexeme(REGULAR_ASSIGNMENT, currentLexeme.getLineNumber());
        if (check(ASSIGN_OPERATOR)) regularAssignment.addChild(consume(ASSIGN_OPERATOR));
        else if (operatorAssignmentPending()) regularAssignment.addChild(operatorAssignment());
        else error("Expected assignment operator.");
        regularAssignment.addChild(expression());
        return regularAssignment;
    }

    private Lexeme functionArgs() {
        log("functionArgs");
        Lexeme functionArgs = new Lexeme(FUNCTION_ARGS, currentLexeme.getLineNumber());
        while (check(IDENTIFIER)) {
            functionArgs.addChild(consume(IDENTIFIER));
        }
        return functionArgs;
    }

    private Lexeme block() {
        log("block");
        Lexeme block = null;
        consume(DOUBLE_FORWARD);
        if (statementPending()) {
            block = statementList();
        }
        consume(DOUBLE_BACKWARD);
        return block;
    }

    private Lexeme forLoop() {
        log("forLoop");
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
        log("foreachLoop");
        Lexeme foreachLoop = new Lexeme(FOREACH_LOOP, currentLexeme.getLineNumber());
        consume(FOREACH_KEYWORD);
        consume(OPEN_CURLY);
        consume(VAR_KEYWORD);
        foreachLoop.addChild(consume(IDENTIFIER));
        consume(OF_KEYWORD);
        if (check(IDENTIFIER)) foreachLoop.addChild(consume(IDENTIFIER));
        if (arrayPending()) foreachLoop.addChild(array());
        consume(CLOSED_CURLY);
        foreachLoop.addChild(block());
        return foreachLoop;
    }

    private Lexeme whenLoop() {
        log("whenLoop");
        Lexeme whenLoop = new Lexeme(WHEN_LOOP, currentLexeme.getLineNumber());
        consume(WHEN_KEYWORD);
        consume(OPEN_CURLY);
        whenLoop.addChild(expression());
        consume(CLOSED_CURLY);
        whenLoop.addChild(block());
        return whenLoop;
    }

    private Lexeme loopLoop() {
        log("loopLoop");
        Lexeme loopLoop = new Lexeme(LOOP_LOOP, currentLexeme.getLineNumber());
        consume(LOOP_KEYWORD);
        consume(OPEN_CURLY);
        loopLoop.addChild(expression());
        consume(CLOSED_CURLY);
        loopLoop.addChild(block());
        return loopLoop;
    }

    private Lexeme butifStatementList() {
        log("butifStatementList");
        Lexeme butifStatementList = new Lexeme(BUTIF_STATEMENT_LIST, currentLexeme.getLineNumber());
        while (butifStatementPending()) {
            butifStatementList.addChild(butifStatement());
        }
        return butifStatementList;
    }

    private Lexeme butifStatement() {
        log("butifStatement");
        Lexeme butIfStatement = new Lexeme(BUTIF_STATEMENT, currentLexeme.getLineNumber());
        consume(BUTIF_KEYWORD);
        consume(OPEN_CURLY);
        butIfStatement.addChild(expression());
        consume(CLOSED_CURLY);

        consume(DOUBLE_FORWARD);
        if (statementPending()) {
            butIfStatement.addChild(statementList());
        } else {
            butIfStatement.addChild(new Lexeme(STATEMENT_LIST, currentLexeme.getLineNumber()));
        }
        consume(DOUBLE_BACKWARD);

        return butIfStatement;
    }

    private Lexeme butStatement() {
        log("butStatement");
        Lexeme butStatement = new Lexeme(BUT_STATEMENT, currentLexeme.getLineNumber());
        consume(BUT_KEYWORD);

        consume(DOUBLE_FORWARD);
        if (statementPending()) {
            butStatement.addChild(statementList());
        } else {
            butStatement.addChild(new Lexeme(STATEMENT_LIST, currentLexeme.getLineNumber()));
        }
        consume(DOUBLE_BACKWARD);

        return butStatement;
    }

    private Lexeme changeStatement() {
        log("changeStatement");
        Lexeme changeStatement = new Lexeme(CHANGE_STATEMENT, currentLexeme.getLineNumber());
        consume(CHANGE_KEYWORD);
        consume(OPEN_CURLY);
        changeStatement.addChild(consume(IDENTIFIER));
        consume(CLOSED_CURLY);
        consume(DOUBLE_FORWARD);
        changeStatement.addChild(changeCases());
        consume(DOUBLE_BACKWARD);
        return changeStatement;
    }

    private Lexeme changeCases() {
        log("cases");
        Lexeme changeCases = new Lexeme(CHANGE_CASES, currentLexeme.getLineNumber());
        changeCases.addChild(changeCase());
        while (changeCasePending()) {
            changeCases.addChild(changeCase());
        }
        if (noCasePending()) {
            changeCases.addChild(noCase());
        } else {
            changeCases.addChild(new Lexeme(CHANGE_CASE, currentLexeme.getLineNumber()));
        }
        return changeCases;
    }

    private Lexeme changeCase() {
        log("case");
        Lexeme changeCase = new Lexeme(CHANGE_CASE, currentLexeme.getLineNumber());
        consume(CASE_KEYWORD);
        consume(OPEN_CURLY);
        changeCase.addChild(expression());
        consume(CLOSED_CURLY);
        changeCase.addChild(block());
        return changeCase;
    }

    private Lexeme noCase() {
        log("nocase");
        Lexeme noCase = new Lexeme(CHANGE_CASE, currentLexeme.getLineNumber());
        consume(NOCASE_KEYWORD);
        noCase.addChild(block());
        return noCase;
    }

    private Lexeme operatorAssignment() {
        log("operatorAssignment");
        if (check(PLUS_ASSIGNMENT)) return consume(PLUS_ASSIGNMENT);
        else if (check(MINUS_ASSIGNMENT)) return consume(MINUS_ASSIGNMENT);
        else if (check(TIMES_ASSIGNMENT)) return consume(TIMES_ASSIGNMENT);
        else if (check(TIMES_ASSIGNMENT)) return consume(TIMES_ASSIGNMENT);
        else if (check(DIVIDE_ASSIGNMENT)) return consume(DIVIDE_ASSIGNMENT);
        else if (check(DOUBLE_DIVIDE_ASSIGNMENT)) return consume(DOUBLE_DIVIDE_ASSIGNMENT);
        else if (check(CARET_ASSIGNMENT)) return consume(CARET_ASSIGNMENT);
        else if (check(PERCENT_ASSIGNMENT)) return consume(PERCENT_ASSIGNMENT);
        else error("Expected assignment operator.");
        return null;
    }

    private Lexeme array() {
        log("array");
        consume(OPEN_PAREN);
        ArrayList<Lexeme> arrayList = new ArrayList<>();
        while (primaryPending()) {
            arrayList.add(primary());
        }
        consume(CLOSED_PAREN);
        return new Lexeme(ARRAY, currentLexeme.getLineNumber(), arrayList);
    }

    private Lexeme cast() {
        log("cast");
        Lexeme cast = new Lexeme(CAST, currentLexeme.getLineNumber());
        cast.addChild(type());
        consume(DOT);
        if (check(NUMBER)) {
            cast.addChild(consume(NUMBER));
        } else if (check(IDENTIFIER)) {
            cast.addChild(consume(IDENTIFIER));
        } else if (check(STRING)) {
            cast.addChild(consume(STRING));
        } else if (check(BOOLEAN)) {
            cast.addChild(consume(BOOLEAN));
        } else if (arrayPending()) {
            cast.addChild(array());
        } else {
            error("Unexpected type casting.");
            return null;
        }
        return cast;
    }

    private Lexeme functionCall() {
        log("functionCall");
        Lexeme functionCall = new Lexeme(FUNCTION_CALL, currentLexeme.getLineNumber());
        functionCall.addChild(consume(IDENTIFIER));
        consume(OPEN_CURLY);
        functionCall.addChild(callArguments());
        consume(CLOSED_CURLY);
        return functionCall;
    }

    private Lexeme callArguments() {
        log("callArguments");
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
        return (check(IDENTIFIER) && regularAssignmentPendingNext()) || ((check(INCREMENT) || check(DECREMENT)) && checkNext(IDENTIFIER));
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

    private boolean noCasePending() {
        return check(NOCASE_KEYWORD);
    }

    private boolean commentPending() {
        return check(COMMENT);
    }

    private boolean expressionPending() {
        return orExprPending();
    }

    private boolean orExprPending() {
        return xorExprPending();
    }

    private boolean xorExprPending() {
        return andExprPending();
    }

    private boolean andExprPending() {
        return weakEqualityExprPending();
    }

    private boolean weakEqualityExprPending() {
        return equalityExprPending();
    }

    private boolean equalityExprPending() {
        return comparisonExprPending();
    }

    private boolean comparisonExprPending() {
        return sumExprPending();
    }

    private boolean sumExprPending() {
        return productExprPending();
    }

    private boolean productExprPending() {
        return exponentExprPending();
    }

    private boolean exponentExprPending() {
        return unaryExprPending();
    }

    private boolean unaryExprPending() {
        return primaryPending() || check(EXCLAMATION) || check(NOT_KEYWORD) || check(MINUS) || check(INCREMENT) || check(DECREMENT);
    }

    private boolean primaryPending() {
        return check(NUMBER) || check(IDENTIFIER) || check(STRING) || check(BOOLEAN) || check(NOTHING_KEYWORD) || arrayPending() || castPending() || functionCallPending() || parenthesizedExpressionPending();
    }

    private boolean parenthesizedExpressionPending() {
        return check(OPEN_CURLY);
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

    private boolean arrayPending() {
        return check(OPEN_PAREN);
    }

    private boolean castPending() {
        return typePending() && checkNext(DOT);
    }

    private boolean typePending() {
        return check(STR_KEYWORD) || check(NUM_KEYWORD) || check(TF_KEYWORD) || check(ARR_KEYWORD);
    }

    private boolean functionCallPending() {
        return check(IDENTIFIER) && checkNext(OPEN_CURLY);
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