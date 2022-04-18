package com.sigma.evaluation;

import com.sigma.Sigma;
import com.sigma.environments.Environment;
import com.sigma.lexicalAnalysis.Lexeme;
import com.sigma.lexicalAnalysis.TokenType;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static com.sigma.lexicalAnalysis.TokenType.*;

// TODO take (return), end (break), fall (continue), length, array get/set, other built ins
// TODO fix function arguments? comma separated?

public class Evaluator {
    private static final boolean printDebugMessages = false;

    private static final ArrayList<Lexeme> output = new ArrayList<>();

    public void print() {
        for (Lexeme lexeme : output) {
            if (lexeme != null) System.out.println(lexeme);
        }
    }

    public Lexeme eval(Lexeme tree, Environment environment) {
        if (tree == null) return null;

        return switch (tree.getType()) {
            case PROGRAM -> eval(tree.getChild(0), environment);
            case STATEMENT_LIST -> evalStatementList(tree, environment);

            case EXPRESSION -> eval(tree.getChild(0), environment);

            case VARIABLE_DECLARATION -> evalVariableDeclaration(tree, environment);
            case FUNCTION_DEFINITION -> evalFunctionDefinition(tree, environment);

            case FUNCTION_CALL -> evalFunctionCall(tree, environment);

            case IF_STATEMENT -> evalIfStatement(tree, environment);

            case CHANGE_STATEMENT -> evalChangeStatement(tree, environment);

            case FOR_LOOP -> evalForLoop(tree, environment);
            case FOREACH_LOOP -> evalForeachLoop(tree, environment);
            case WHEN_LOOP -> evalWhenLoop(tree, environment);
            case LOOP_LOOP -> evalLoopLoop(tree, environment);

            case ASSIGNMENT -> evalAssignment(tree, environment);

            case PLUS, MINUS, TIMES, DIVIDE, DOUBLE_DIVIDE, CARET, PERCENT -> evalSimpleBinaryOperator(tree, environment);
            case EXCLAMATION, NOT_KEYWORD -> evalNotOperator(tree, environment);
            case INCREMENT, DECREMENT -> evalIncDec(tree, environment);
            case QUESTION, APPROX, GREATER, LESS, GEQ, LEQ,
                    NOT_QUESTION, NOT_APPROX,
                    DOUBLE_QUESTION, GREATER_QUESTION, LESS_QUESTION,
                    NOT_DOUBLE_QUESTION -> evalBinaryComparator(tree, environment);
            case AND_KEYWORD, OR_KEYWORD,
                    NAND_KEYWORD, NOR_KEYWORD, XOR_KEYWORD, XNOR_KEYWORD -> evalBooleanBinaryOperator(tree, environment);

            case NUMBER, STRING, BOOLEAN, ARRAY, NOTHING_KEYWORD -> tree;
            case COMMENT -> null;
            case IDENTIFIER -> environment.lookup(tree);
            case CAST -> evalCast(tree, environment);

            default -> null;
        };
    }

    private Lexeme evalStatementList(Lexeme tree, Environment environment) {
        log("evalStatementList");
        for (int i = 0; i < tree.getNumChildren(); i++) {
            output.add(eval(tree.getChild(i), environment));
        }
        return null;
    }

    private Lexeme evalVariableDeclaration(Lexeme tree, Environment environment) {
        environment.add(tree.getChild(0), eval(tree.getChild(1), environment));
        return null;
    }

    private Lexeme evalFunctionDefinition(Lexeme tree, Environment environment) {
        tree.setDefiningEnvironment(environment);
        environment.add(tree.getChild(0), tree);
        return null;
    }

    private Lexeme evalFunctionCall(Lexeme tree, Environment environment) {
        Lexeme functionName = tree.getChild(0);
        Lexeme closure = environment.lookup(functionName);
        if (closure.getType() != FUNCTION_DEFINITION)
            error("Attempt to call " + closure.getType() + " as function failed", functionName);
        Environment definingEnv = closure.getDefiningEnvironment();
        Environment callEnv = new Environment(definingEnv);
        Lexeme paramList = closure.getChild(1);
        Lexeme argList = tree.getChild(1);
        Lexeme evalArgList = evalArgumentList(argList, environment);
        callEnv.extend(paramList, evalArgList);
        Lexeme functionBody = closure.getChild(2);
        return eval(functionBody, callEnv);
    }

    private Lexeme evalArgumentList(Lexeme tree, Environment environment) {
        Lexeme evaluated = new Lexeme(CALL_ARGUMENTS, tree.getLineNumber());
        for (int i = 0; i < tree.getNumChildren(); i++) {
            evaluated.addChild(eval(tree.getChild(i), environment));
        }
        return evaluated;
    }

    private Lexeme evalAssignment(Lexeme tree, Environment environment) {
        Lexeme id = tree.getChild(0);
        Lexeme one = new Lexeme(NUMBER, tree.getLineNumber(), 1);
        switch (tree.getChild(1).getType()) {
            case INCREMENT -> {
                Lexeme plus = new Lexeme(PLUS, tree.getLineNumber());
                plus.addChild(id);
                plus.addChild(one);
                Lexeme result = evalPlus(plus, environment);
                environment.update(id, result);
            }
            case DECREMENT -> {
                Lexeme minus = new Lexeme(MINUS, tree.getLineNumber());
                minus.addChild(id);
                minus.addChild(one);
                Lexeme result = evalMinus(minus, environment);
                environment.update(id, result);
            }
            case REGULAR_ASSIGNMENT -> {
                return evalRegularAssignment(tree, environment);
            }
            default -> {
                error("Problem with variable assignment", tree);
                return null;
            }
        }
        return null;
    }

    private Lexeme evalRegularAssignment(Lexeme tree, Environment environment) {
        Lexeme id = tree.getChild(0);
        Lexeme op = tree.getChild(1).getChild(0);
        Lexeme exp = tree.getChild(1).getChild(1);
        switch (op.getType()) {
            case ASSIGN_OPERATOR -> {
                Lexeme result = eval(exp, environment);
                environment.update(id, result);
            }
            case PLUS_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(PLUS, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalPlus(newTree, environment);
                environment.update(id, result);
            }
            case MINUS_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(MINUS, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalMinus(newTree, environment);
                environment.update(id, result);
            }
            case TIMES_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(TIMES, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalTimes(newTree, environment);
                environment.update(id, result);
            }
            case DIVIDE_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(DIVIDE, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalDivide(newTree, environment);
                environment.update(id, result);
            }
            case DOUBLE_DIVIDE_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(DOUBLE_DIVIDE, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalDoubleDivide(newTree, environment);
                environment.update(id, result);
            }
            case CARET_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(CARET, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalCaret(newTree, environment);
                environment.update(id, result);
            }
            case PERCENT_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(PERCENT, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalPercent(newTree, environment);
                environment.update(id, result);
            }
            default -> {
                error("Problem with assignment operator", tree);
                return null;
            }
        }
        return null;
    }

    private Lexeme evalIfStatement(Lexeme tree, Environment environment) {
        Lexeme ifExp = eval(tree.getChild(0), environment);
        if (isTruthy(ifExp)) {
            Environment ifEnv = new Environment(environment);
            eval(tree.getChild(1), ifEnv);
            return null;
        }
        for (int i = 0; i < tree.getChild(2).getNumChildren(); i++) {
            Lexeme butIfExp = eval(tree.getChild(2).getChild(i).getChild(0), environment);
            if (isTruthy(butIfExp)) {
                Environment butIfEnv = new Environment(environment);
                eval(tree.getChild(2).getChild(i).getChild(1), butIfEnv);
                return null;
            }
        }
        if (tree.getNumChildren() == 4) {
            Environment butEnv = new Environment(environment);
            eval(tree.getChild(3).getChild(0), butEnv);
        }
        return null;
    }

    private Lexeme evalChangeStatement(Lexeme tree, Environment environment) {
        if (tree.getChild(0) == null) {
            error("Missing identifier", tree);
            return null;
        }
        Lexeme value = environment.lookup(tree.getChild(0));
        boolean caught = false;
        for (int i = 0; i < tree.getChild(1).getNumChildren() - 1; i++) {
            Lexeme op = new Lexeme(QUESTION, tree.getLineNumber());
            op.addChild(value);
            if (eval(tree.getChild(1).getChild(i).getChild(0), environment) == null) {
                error("Missing expression", tree.getChild(1).getChild(i));
                return null;
            } else {
                op.addChild(eval(tree.getChild(1).getChild(i).getChild(0), environment));
                Lexeme evaluated = evalBinaryComparator(op, environment);
                if (evaluated == null) {
                    error("Error calculating change statement", tree.getChild(1).getChild(i).getChild(0));
                    return null;
                } else if (isTruthy(evaluated)) {
                    Environment caseEnvironment = new Environment(environment);
                    eval(tree.getChild(1).getChild(i).getChild(1), caseEnvironment);
                    caught = true;
                    break;
                }
            }
        }
        if (!caught) {
            Environment caseEnvironment = new Environment(environment);
            eval(tree.getChild(1).getChild(tree.getChild(1).getNumChildren() - 1).getChild(0), caseEnvironment);
        }
        return null;
    }

    private Lexeme evalForLoop(Lexeme tree, Environment environment) {
        Environment forEnvironment = new Environment(environment);
        forEnvironment.add(tree.getChild(0).getChild(0), eval(tree.getChild(0).getChild(1), forEnvironment));
        Lexeme count = new Lexeme(IDENTIFIER, tree.getLineNumber(), "count");
        forEnvironment.add(count, new Lexeme(NUMBER, tree.getLineNumber(), 0));
        while (isTruthy(eval(tree.getChild(1), forEnvironment))) {
            Environment forBody = new Environment(forEnvironment);
            eval(tree.getChild(3), forBody);
            forEnvironment.update(count, new Lexeme(NUMBER, tree.getLineNumber(), forEnvironment.lookup(count).getNumVal() + 1));
            eval(tree.getChild(2), forEnvironment);
        }
        return null;
    }

    private Lexeme evalForeachLoop(Lexeme tree, Environment environment) {
        ArrayList<Lexeme> foreachArray;
        foreachArray = eval(tree.getChild(1), environment).arrayVal;
        Environment foreachEnvironment = new Environment(environment);
        Lexeme count = new Lexeme(IDENTIFIER, tree.getLineNumber(), "count");
        foreachEnvironment.add(count, new Lexeme(NUMBER, tree.getLineNumber(), 0));
        for (Lexeme lexeme : foreachArray) {
            Environment foreachBody = new Environment(foreachEnvironment);
            foreachBody.add(tree.getChild(0), lexeme);
            eval(tree.getChild(2), foreachBody);
            foreachEnvironment.update(count, new Lexeme(NUMBER, tree.getLineNumber(), foreachEnvironment.lookup(count).getNumVal() + 1));
        }
        return null;
    }

    private Lexeme evalWhenLoop(Lexeme tree, Environment environment) {
        Environment whenEnvironment = new Environment(environment);
        Lexeme count = new Lexeme(IDENTIFIER, tree.getLineNumber(), "count");
        whenEnvironment.add(count, new Lexeme(NUMBER, tree.getLineNumber(), 0));
        while (isTruthy(eval(tree.getChild(0), environment))) {
            Environment whenBody = new Environment(whenEnvironment);
            eval(tree.getChild(1), whenBody);
            whenEnvironment.update(count, new Lexeme(NUMBER, tree.getLineNumber(), whenEnvironment.lookup(count).getNumVal() + 1));
        }
        return null;
    }

    private Lexeme evalLoopLoop(Lexeme tree, Environment environment) {
        if (eval(tree.getChild(0), environment) == null) {
            error("Missing expression", tree.getChild(0));
            return null;
        }
        Environment loopEnvironment = new Environment(environment);
        Lexeme count = new Lexeme(IDENTIFIER, tree.getLineNumber(), "count");
        loopEnvironment.add(count, new Lexeme(NUMBER, tree.getLineNumber(), 0));
        Lexeme op = new Lexeme(LESS, tree.getLineNumber());
        op.addChild(loopEnvironment.lookup(count));
        op.addChild(tree.getChild(0));
        while (evalBinaryComparator(op, loopEnvironment) != null && isTruthy(evalBinaryComparator(op, loopEnvironment))) {
            eval(tree.getChild(1), loopEnvironment);
            loopEnvironment.update(count, new Lexeme(NUMBER, tree.getLineNumber(), loopEnvironment.lookup(count).getNumVal() + 1));
        }
        return null;
    }

    private Lexeme evalSimpleBinaryOperator(Lexeme tree, Environment environment) {
        log("evalBinaryOperator");
        switch (tree.getType()) {
            case PLUS -> {
                return evalPlus(tree, environment);
            }
            case MINUS -> {
                return evalMinus(tree, environment);
            }
            case TIMES -> {
                return evalTimes(tree, environment);
            }
            case DIVIDE -> {
                return evalDivide(tree, environment);
            }
            case CARET -> {
                return evalCaret(tree, environment);
            }
            case PERCENT -> {
                return evalPercent(tree, environment);
            }
            default -> {
                error("Unrecognized operator: " + tree.getType(), tree);
                return null;
            }
        }
    }

    private Lexeme evalBinaryComparator(Lexeme tree, Environment environment) {
        Lexeme left = eval(tree.getChild(0), environment);
        Lexeme right = eval(tree.getChild(1), environment);
        TokenType lType = left.getType();
        TokenType rType = right.getType();
        int lLevel;
        double lValue;
        int rLevel;
        double rValue;
        boolean result;
        switch (lType) {
            case NUMBER -> {
                lLevel = 0;
                lValue = left.getNumVal();
            }
            case STRING -> {
                lLevel = 0;
                lValue = left.getStringVal().length();
            }
            case BOOLEAN -> {
                lLevel = 0;
                lValue = left.getBoolVal() ? 1 : 0;
            }
            case ARRAY -> {
                lLevel = 1;
                lValue = left.arrayVal.size();
            }
            default -> {
                error("Could not calculate binary comparison", tree);
                return null;
            }
        }
        switch (rType) {
            case NUMBER -> {
                rLevel = 0;
                rValue = right.getNumVal();
            }
            case STRING -> {
                rLevel = 0;
                rValue = right.getStringVal().length();
            }
            case BOOLEAN -> {
                rLevel = 0;
                rValue = right.getBoolVal() ? 1 : 0;
            }
            case ARRAY -> {
                rLevel = 1;
                rValue = right.arrayVal.size();
            }
            default -> {
                error("Could not calculate binary comparison", tree);
                return null;
            }
        }
        final boolean approx = Math.abs(lValue - rValue) < (lValue + rValue) / 2 * 0.05;
        switch (tree.getType()) {
            case QUESTION -> {
                result = lLevel == rLevel && lValue == rValue;
            }
            case NOT_QUESTION -> {
                result = !(lLevel == rLevel && lValue == rValue);
            }
            case APPROX -> {
                result = lLevel == rLevel && approx;
            }
            case NOT_APPROX -> {
                result = !(lLevel == rLevel && approx);
            }
            case DOUBLE_QUESTION -> {
                result = lType == rType;
            }
            case NOT_DOUBLE_QUESTION -> {
                result = !(lType == rType);
            }
            case GREATER -> {
                if (lLevel > rLevel) {
                    result = true;
                } else if (lLevel < rLevel) {
                    result = false;
                } else {
                    result = lValue > rValue;
                }
            }
            case LESS -> {
                if (lLevel > rLevel) {
                    result = false;
                } else if (lLevel < rLevel) {
                    result = true;
                } else {
                    result = lValue < rValue;
                }
            }
            case GEQ, GREATER_QUESTION -> {
                if (lLevel > rLevel) {
                    result = true;
                } else if (lLevel < rLevel) {
                    result = false;
                } else {
                    result = lValue >= rValue;
                }
            }
            case LEQ, LESS_QUESTION -> {
                if (lLevel > rLevel) {
                    result = false;
                } else if (lLevel < rLevel) {
                    result = true;
                } else {
                    result = lValue <= rValue;
                }
            }
            default -> {
                error("Could not calculate binary comparison", tree);
                return null;
            }
        }
        return new Lexeme(BOOLEAN, tree.getLineNumber(), result);
    }

    private Lexeme evalBooleanBinaryOperator(Lexeme tree, Environment environment) {
        Lexeme left = eval(tree.getChild(0), environment);
        Lexeme right = eval(tree.getChild(1), environment);
        boolean l = isTruthy(left);
        boolean r = isTruthy(right);
        boolean result;
        switch (tree.getType()) {
            case AND_KEYWORD -> {
                result = l && r;
            }
            case OR_KEYWORD -> {
                result = l || r;
            }
            case NAND_KEYWORD -> {
                result = !(l && r);
            }
            case XOR_KEYWORD -> {
                result = l ^ r;
            }
            case XNOR_KEYWORD -> {
                result = l == r;
            }
            default -> {
                error("Problem with binary boolean operator", tree);
                return null;
            }
        }
        return new Lexeme(BOOLEAN, tree.getLineNumber(), result);
    }

    private Lexeme evalIncDec(Lexeme tree, Environment environment) {
        Lexeme value = tree.getChild(0);
        Lexeme one = new Lexeme(NUMBER, tree.getLineNumber(), 1);
        switch (tree.getType()) {
            case INCREMENT -> {
                Lexeme plus = new Lexeme(PLUS, tree.getLineNumber());
                plus.addChild(value);
                plus.addChild(one);
                Lexeme result = evalPlus(plus, environment);
                environment.update(value, result);
                return result;
            }
            case DECREMENT -> {
                Lexeme minus = new Lexeme(MINUS, tree.getLineNumber());
                minus.addChild(value);
                minus.addChild(one);
                Lexeme result = evalMinus(minus, environment);
                environment.update(value, result);
                return result;
            }
            default -> {
                error("Incorrect increment/decrement type given", tree);
                return null;
            }
        }
    }

    private Lexeme evalPlus(Lexeme tree, Environment environment) {
        log("evalPlus");
        Lexeme l = eval(tree.getChild(0), environment);
        Lexeme r = eval(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        if (lType == NOTHING_KEYWORD || rType == NOTHING_KEYWORD) {
            error("Could not calculate binary operation with nothing keyword", tree);
            return null;
        }

        switch (lType) {
            case NUMBER:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() + r.getNumVal());
                    case STRING:
                        return new Lexeme(STRING, tree.getLineNumber(), l.getNumVal() + r.getStringVal());
                    case BOOLEAN:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() + (r.getBoolVal() ? 1 : 0));
                    case ARRAY:
                        ArrayList<Lexeme> tempL = l.arrayVal;
                        tempL.add(0, r);
                        return new Lexeme(ARRAY, tree.getLineNumber(), tempL);
                    default:
                        error("Could not calculate plus operation", tree);
                        return null;
                }
            case STRING:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal() + r.getNumVal());
                    case STRING:
                        return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal() + r.getStringVal());
                    case BOOLEAN:
                        return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal() + (r.getBoolVal() ? "true" : "fals"));
                    case ARRAY:
                        ArrayList<Lexeme> tempL = l.arrayVal;
                        tempL.add(0, r);
                        return new Lexeme(ARRAY, tree.getLineNumber(), tempL);
                    default:
                        error("Could not calculate plus operation", tree);
                        return null;
                }
            case BOOLEAN:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), (l.getBoolVal() ? 1 : 0) + r.getNumVal());
                    case STRING:
                        return new Lexeme(STRING, tree.getLineNumber(), (l.getBoolVal() ? "true" : "fals") + r.getStringVal());
                    case BOOLEAN:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal() || r.getBoolVal());
                    case ARRAY:
                        ArrayList<Lexeme> tempL = l.arrayVal;
                        tempL.add(0, r);
                        return new Lexeme(ARRAY, tree.getLineNumber(), tempL);
                    default:
                        error("Could not calculate plus operation", tree);
                        return null;
                }
            case ARRAY:
                switch (rType) {
                    case NUMBER, STRING, BOOLEAN -> {
                        ArrayList<Lexeme> tempL = l.arrayVal;
                        tempL.add(r);
                        return new Lexeme(ARRAY, tree.getLineNumber(), tempL);
                    }
                    case ARRAY -> {
                        ArrayList<Lexeme> tempL = l.arrayVal;
                        tempL.addAll(r.arrayVal);
                        return new Lexeme(ARRAY, tree.getLineNumber(), tempL);
                    }
                    default -> {
                        error("Could not calculate plus operation", tree);
                        return null;
                    }
                }
            default:
                error("Could not calculate plus operation", tree);
                return null;
        }
    }

    private Lexeme evalMinus(Lexeme tree, Environment environment) {
        log("evalMinus");
        if (tree.getNumChildren() == 1) {
            Lexeme child = eval(tree.getChild(0), environment);
            TokenType type = child.getType();
            switch (type) {
                case NUMBER:
                    return new Lexeme(NUMBER, tree.getLineNumber(), -child.getNumVal());
                case STRING:
                    return new Lexeme(STRING, tree.getLineNumber(), new StringBuilder(child.getStringVal()).reverse().toString());
                case BOOLEAN:
                    return new Lexeme(BOOLEAN, tree.getLineNumber(), !child.getBoolVal());
                case ARRAY:
                    ArrayList<Lexeme> temp = child.arrayVal;
                    Collections.reverse(temp);
                    return new Lexeme(ARRAY, tree.getLineNumber(), temp);
                case NOTHING_KEYWORD:
                    return child;
                default:
                    error("Invalid type after unary minus operator", tree);
                    return null;
            }
        } else if (tree.getNumChildren() == 2) {
            Lexeme l = eval(tree.getChild(0), environment);
            Lexeme r = eval(tree.getChild(1), environment);
            TokenType lType = l.getType();
            TokenType rType = r.getType();

            if (lType == NOTHING_KEYWORD || rType == NOTHING_KEYWORD) {
                error("Could not calculate binary operation with nothing keyword", tree);
                return null;
            }

            switch (lType) {
                case NUMBER:
                    switch (rType) {
                        case NUMBER:
                            return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() - r.getNumVal());
                        case STRING:
                            return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() - r.getStringVal().length());
                        case BOOLEAN:
                            return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() - (r.getBoolVal() ? 1 : 0));
                        case ARRAY:
                            ArrayList<Lexeme> tempR = r.arrayVal;
                            Collections.reverse(tempR);
                            tempR.removeIf(i -> i.equals(l));
                            return new Lexeme(ARRAY, tree.getLineNumber(), tempR);
                        default:
                            error("Could not calculate minus operation", tree);
                            return null;
                    }
                case STRING:
                    switch (rType) {
                        case NUMBER:
                            return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal().substring(0, Math.max(0, (int) (l.getStringVal().length() - Math.floor(r.getNumVal())))));
                        case STRING:
                            return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal().replaceAll(r.getStringVal(), ""));
                        case BOOLEAN:
                            return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal().substring(0, l.getStringVal().length() - (r.getBoolVal() ? 1 : 0)));
                        case ARRAY:
                            ArrayList<Lexeme> tempR = r.arrayVal;
                            Collections.reverse(tempR);
                            tempR.removeIf(i -> i.equals(l));
                            return new Lexeme(ARRAY, tree.getLineNumber(), tempR);
                        default:
                            error("Could not calculate minus operation", tree);
                            return null;
                    }
                case BOOLEAN:
                    switch (rType) {
                        case NUMBER:
                            return new Lexeme(NUMBER, tree.getLineNumber(), (l.getBoolVal() ? 1 : 0) - r.getNumVal());
                        case STRING:
                            return new Lexeme(STRING, tree.getLineNumber(), (l.getBoolVal() ? "true" : "fals").replaceAll(r.getStringVal(), ""));
                        case BOOLEAN:
                            return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal() ^ r.getBoolVal());
                        case ARRAY:
                            ArrayList<Lexeme> tempR = r.arrayVal;
                            Collections.reverse(tempR);
                            tempR.removeIf(i -> i.equals(l));
                            return new Lexeme(ARRAY, tree.getLineNumber(), tempR);
                        default:
                            error("Could not calculate minus operation", tree);
                            return null;
                    }
                case ARRAY:
                    switch (rType) {
                        case NUMBER, STRING, BOOLEAN -> {
                            ArrayList<Lexeme> tempL = l.arrayVal;
                            tempL.removeIf(i -> i.equals(r));
                            return new Lexeme(ARRAY, tree.getLineNumber(), tempL);
                        }
                        case ARRAY -> {
                            ArrayList<Lexeme> tempL = l.arrayVal;
                            for (Lexeme lexeme : r.arrayVal) {
                                tempL.removeIf(i -> i.equals(lexeme));
                            }
                            return new Lexeme(ARRAY, tree.getLineNumber(), tempL);
                        }
                        default -> {
                            error("Could not calculate minus operation", tree);
                            return null;
                        }
                    }
                default:
                    error("Could not calculate minus operation", tree);
                    return null;
            }
        } else {
            error("Invalid use of minus operator", tree);
            return null;
        }
    }

    private Lexeme evalNotOperator(Lexeme tree, Environment environment) {
        Lexeme child = eval(tree.getChild(0), environment);
        TokenType type = child.getType();
        switch (type) {
            case NUMBER:
                return new Lexeme(BOOLEAN, tree.getLineNumber(), child.getNumVal() == 0);
            case STRING:
                return new Lexeme(BOOLEAN, tree.getLineNumber(), child.getStringVal().equals(""));
            case BOOLEAN:
                return new Lexeme(BOOLEAN, tree.getLineNumber(), !child.getBoolVal());
            case ARRAY:
                return new Lexeme(BOOLEAN, tree.getLineNumber(), child.arrayVal.size() == 0);
            case NOTHING_KEYWORD:
                return child;
            default:
                error("Invalid type after not operator", tree);
                return null;
        }
    }

    private Lexeme evalTimes(Lexeme tree, Environment environment) {
        log("evalTimes");
        Lexeme l = eval(tree.getChild(0), environment);
        Lexeme r = eval(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        if (lType == NOTHING_KEYWORD || rType == NOTHING_KEYWORD) {
            error("Could not calculate binary operation with nothing keyword", tree);
            return null;
        }

        switch (lType) {
            case NUMBER:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() * r.getNumVal());
                    case STRING:
                        return new Lexeme(STRING, tree.getLineNumber(), r.getStringVal().repeat((int) Math.floor(l.getNumVal())) + r.getStringVal().substring(0, (int) (r.getStringVal().length() * (l.getNumVal() % 1))));
                    case BOOLEAN:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), r.getBoolVal());
                    case ARRAY:
                        ArrayList<Lexeme> tempR = new ArrayList<>();
                        for (int i = 0; i <= l.getNumVal() - 1; i++) {
                            tempR.addAll(r.arrayVal);
                        }
                        tempR.addAll(r.arrayVal.subList(0, (int) (l.getNumVal() % 1)));
                        return new Lexeme(ARRAY, tree.getLineNumber(), tempR);
                    default:
                        error("Could not calculate times operation", tree);
                        return null;
                }
            case STRING:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal().repeat((int) Math.floor(r.getNumVal())) + l.getStringVal().substring(0, (int) (l.getStringVal().length() * (r.getNumVal() % 1))));
                    case STRING:
                        char[] tempArray = (l.getStringVal() + r.getStringVal()).toCharArray();
                        Arrays.sort(tempArray);
                        return new Lexeme(STRING, tree.getLineNumber(), new String(tempArray));
                    case BOOLEAN:
                        return new Lexeme(STRING, tree.getLineNumber(), r.getBoolVal() ? l.getStringVal() : "");
                    case ARRAY:
                        ArrayList<Lexeme> tempR = new ArrayList<>();
                        for (int i = 0; i < l.getStringVal().length(); i++) {
                            tempR.addAll(r.arrayVal);
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), tempR);
                    default:
                        error("Could not calculate times operation", tree);
                        return null;
                }
            case BOOLEAN:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getBoolVal());
                    case STRING:
                        return new Lexeme(STRING, tree.getLineNumber(), l.getBoolVal() ? r.getStringVal() : "");
                    case BOOLEAN:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal() && r.getBoolVal());
                    case ARRAY:
                        return new Lexeme(ARRAY, tree.getLineNumber(), l.getBoolVal() ? r.arrayVal : new ArrayList<Lexeme>());
                    default:
                        error("Could not calculate times operation", tree);
                        return null;
                }
            case ARRAY:
                ArrayList<Lexeme> tempL = new ArrayList<>();
                switch (rType) {
                    case NUMBER:
                        for (int i = 0; i <= r.getNumVal() - 1; i++) {
                            tempL.addAll(l.arrayVal);
                        }
                        tempL.addAll(l.arrayVal.subList(0, (int) (r.getNumVal() % 1)));
                        return new Lexeme(ARRAY, tree.getLineNumber(), tempL);
                    case STRING:
                        for (int i = 0; i < r.getStringVal().length(); i++) {
                            tempL.addAll(l.arrayVal);
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), tempL);
                    case BOOLEAN:
                        return new Lexeme(ARRAY, tree.getLineNumber(), l.getBoolVal() ? r.arrayVal : new ArrayList<Lexeme>());
                    case ARRAY:
                        tempL = l.arrayVal;
                        tempL.addAll(r.arrayVal);
                        ArrayList<Lexeme> newList = new ArrayList<Lexeme>();
                        for (Lexeme lexeme : tempL) {
                            boolean duplicate = false;
                            for (Lexeme newLexeme : newList) {
                                if (newLexeme.equals(lexeme)) duplicate = true;
                                break;
                            }
                            if (!duplicate) newList.add(lexeme);
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    default:
                        error("Could not calculate times operation", tree);
                        return null;
                }
            default:
                error("Could not calculate times operation", tree);
                return null;
        }
    }

    private Lexeme evalDivide(Lexeme tree, Environment environment) {
        log("evalDivide");
        Lexeme l = eval(tree.getChild(0), environment);
        Lexeme r = eval(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        if (lType == NOTHING_KEYWORD || rType == NOTHING_KEYWORD) {
            error("Could not calculate binary operation with nothing keyword", tree);
            return null;
        }

        switch (lType) {
            case NUMBER:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() / r.getNumVal());
                    case STRING:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() / r.getStringVal().length());
                    case BOOLEAN:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() / (r.getBoolVal() ? 1 : 0));
                    case ARRAY:
                        ArrayList<Lexeme> newList = new ArrayList<>();
                        for (Lexeme lexeme : r.arrayVal) {
                            Lexeme op = new Lexeme(DIVIDE, tree.getLineNumber());
                            op.addChild(l);
                            op.addChild(lexeme);
                            newList.add(evalDivide(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    default:
                        error("Could not calculate divide operation", tree);
                        return null;
                }
            case STRING:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal().substring(0, (int) Math.floor(l.getStringVal().length() / r.getNumVal())));
                    case STRING:
                        return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal().substring(0, l.getStringVal().length() / r.getStringVal().length()));
                    case BOOLEAN:
                        return new Lexeme(STRING, tree.getLineNumber(), r.getBoolVal() ? l.getStringVal() : "");
                    case ARRAY:
                        ArrayList<Lexeme> newList = new ArrayList<>();
                        for (Lexeme lexeme : r.arrayVal) {
                            Lexeme op = new Lexeme(DIVIDE, tree.getLineNumber());
                            op.addChild(l);
                            op.addChild(lexeme);
                            newList.add(evalDivide(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    default:
                        error("Could not calculate divide operation", tree);
                        return null;
                }
            case BOOLEAN:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal());
                    case STRING:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal() != (r.getStringVal().equals("")));
                    case BOOLEAN:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal() == r.getBoolVal());
                    case ARRAY:
                        ArrayList<Lexeme> newList = new ArrayList<>();
                        for (Lexeme lexeme : r.arrayVal) {
                            Lexeme op = new Lexeme(DIVIDE, tree.getLineNumber());
                            op.addChild(l);
                            op.addChild(lexeme);
                            newList.add(evalDivide(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    default:
                        error("Could not calculate divide operation", tree);
                        return null;
                }
            case ARRAY:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(ARRAY, tree.getLineNumber(), (ArrayList<Lexeme>) l.arrayVal.subList(0, (int) (l.arrayVal.size() / r.getNumVal())));
                    case STRING:
                        return new Lexeme(ARRAY, tree.getLineNumber(), (ArrayList<Lexeme>) l.arrayVal.subList(0, l.arrayVal.size() / r.getStringVal().length()));
                    case BOOLEAN:
                        return new Lexeme(ARRAY, tree.getLineNumber(), r.getBoolVal() ? l.arrayVal : new ArrayList<>());
                    case ARRAY:
                        return new Lexeme(ARRAY, tree.getLineNumber(), (ArrayList<Lexeme>) l.arrayVal.subList(0, l.arrayVal.size() / r.arrayVal.size()));
                    default:
                        error("Could not calculate divide operation", tree);
                        return null;
                }
            default:
                error("Could not calculate divide operation", tree);
                return null;
        }
    }

    private Lexeme evalDoubleDivide(Lexeme tree, Environment environment) {
        log("evalDoubleDivide");
        Lexeme l = eval(tree.getChild(0), environment);
        Lexeme r = eval(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        if (lType == NOTHING_KEYWORD || rType == NOTHING_KEYWORD) {
            error("Could not calculate binary operation with nothing keyword", tree);
            return null;
        }

        switch (lType) {
            case NUMBER:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), Math.floor(l.getNumVal() / r.getNumVal()));
                    case STRING:
                        return new Lexeme(NUMBER, tree.getLineNumber(), Math.floor(l.getNumVal() / r.getStringVal().length()));
                    case BOOLEAN:
                        return new Lexeme(NUMBER, tree.getLineNumber(), Math.floor(l.getNumVal() / (r.getBoolVal() ? 1 : 0)));
                    case ARRAY:
                        ArrayList<Lexeme> newList = new ArrayList<>();
                        for (Lexeme lexeme : r.arrayVal) {
                            Lexeme op = new Lexeme(DOUBLE_DIVIDE, tree.getLineNumber());
                            op.addChild(l);
                            op.addChild(lexeme);
                            newList.add(evalDoubleDivide(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    default:
                        error("Could not calculate integer divide operation", tree);
                        return null;
                }
            case STRING:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal().substring(0, (int) Math.floor(l.getStringVal().length() / r.getNumVal())));
                    case STRING:
                        return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal().substring(0, l.getStringVal().length() / r.getStringVal().length()));
                    case BOOLEAN:
                        return new Lexeme(STRING, tree.getLineNumber(), r.getBoolVal() ? l.getStringVal() : "");
                    case ARRAY:
                        ArrayList<Lexeme> newList = new ArrayList<>();
                        for (Lexeme lexeme : r.arrayVal) {
                            Lexeme op = new Lexeme(DOUBLE_DIVIDE, tree.getLineNumber());
                            op.addChild(l);
                            op.addChild(lexeme);
                            newList.add(evalDoubleDivide(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    default:
                        error("Could not calculate integer divide operation", tree);
                        return null;
                }
            case BOOLEAN:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal());
                    case STRING:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal() != (r.getStringVal().equals("")));
                    case BOOLEAN:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal() == r.getBoolVal());
                    case ARRAY:
                        ArrayList<Lexeme> newList = new ArrayList<>();
                        for (Lexeme lexeme : r.arrayVal) {
                            Lexeme op = new Lexeme(DOUBLE_DIVIDE, tree.getLineNumber());
                            op.addChild(l);
                            op.addChild(lexeme);
                            newList.add(evalDoubleDivide(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    default:
                        error("Could not calculate integer divide operation", tree);
                        return null;
                }
            case ARRAY:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(ARRAY, tree.getLineNumber(), (ArrayList<Lexeme>) l.arrayVal.subList(0, l.arrayVal.size() / ((int) Math.floor(r.getNumVal()))));
                    case STRING:
                        return new Lexeme(ARRAY, tree.getLineNumber(), (ArrayList<Lexeme>) l.arrayVal.subList(0, l.arrayVal.size() / r.getStringVal().length()));
                    case BOOLEAN:
                        return new Lexeme(ARRAY, tree.getLineNumber(), r.getBoolVal() ? l.arrayVal : new ArrayList<>());
                    case ARRAY:
                        return new Lexeme(ARRAY, tree.getLineNumber(), (ArrayList<Lexeme>) l.arrayVal.subList(0, l.arrayVal.size() / r.arrayVal.size()));
                    default:
                        error("Could not calculate integer divide operation", tree);
                        return null;
                }
            default:
                error("Could not calculate integer divide operation", tree);
                return null;
        }
    }

    private Lexeme evalCaret(Lexeme tree, Environment environment) {
        log("evalCaret");
        Lexeme l = eval(tree.getChild(0), environment);
        Lexeme r = eval(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        if (lType == NOTHING_KEYWORD || rType == NOTHING_KEYWORD) {
            error("Could not calculate binary operation with nothing keyword", tree);
            return null;
        }

        switch (lType) {
            case NUMBER:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), Math.pow(l.getNumVal(), r.getNumVal()));
                    case STRING:
                        return new Lexeme(NUMBER, tree.getLineNumber(), Math.pow(l.getNumVal(), r.getStringVal().length()));
                    case BOOLEAN:
                        return new Lexeme(NUMBER, tree.getLineNumber(), Math.pow(l.getNumVal(), (r.getBoolVal() ? 1 : 0)));
                    case ARRAY:
                        ArrayList<Lexeme> newList = new ArrayList<>();
                        for (Lexeme lexeme : r.arrayVal) {
                            Lexeme op = new Lexeme(CARET, tree.getLineNumber());
                            op.addChild(l);
                            op.addChild(lexeme);
                            newList.add(evalCaret(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    default:
                        error("Could not calculate exponent operation", tree);
                        return null;
                }
            case STRING:
                char[] lArr = l.getStringVal().toCharArray();
                StringBuilder result = new StringBuilder();
                switch (rType) {
                    case NUMBER:
                        for (char i : lArr) {
                            result.append(String.valueOf(i).repeat((int) Math.floor(r.getNumVal())));
                        }
                        return new Lexeme(STRING, tree.getLineNumber(), result.toString());
                    case STRING:
                        for (char i : lArr) {
                            result.append(String.valueOf(i).repeat(r.getStringVal().length()));
                        }
                        return new Lexeme(STRING, tree.getLineNumber(), result.toString());
                    case BOOLEAN:
                        return new Lexeme(STRING, tree.getLineNumber(), r.getBoolVal() ? l.getStringVal() : "");
                    case ARRAY:
                        ArrayList<Lexeme> newList = new ArrayList<>();
                        for (Lexeme lexeme : r.arrayVal) {
                            Lexeme op = new Lexeme(CARET, tree.getLineNumber());
                            op.addChild(l);
                            op.addChild(lexeme);
                            newList.add(evalCaret(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    default:
                        error("Could not calculate exponent operation", tree);
                        return null;
                }
            case BOOLEAN:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal() || r.getNumVal() == 0);
                    case STRING:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal() || r.getStringVal().equals(""));
                    case BOOLEAN:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal() || !r.getBoolVal());
                    case ARRAY:
                        ArrayList<Lexeme> newList = new ArrayList<>();
                        for (Lexeme lexeme : r.arrayVal) {
                            Lexeme op = new Lexeme(CARET, tree.getLineNumber());
                            op.addChild(l);
                            op.addChild(lexeme);
                            newList.add(evalCaret(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    default:
                        error("Could not calculate exponent operation", tree);
                        return null;
                }
            case ARRAY:
                ArrayList<Lexeme> newList = new ArrayList<>();
                switch (rType) {
                    case NUMBER, STRING, BOOLEAN -> {
                        for (Lexeme lexeme : l.arrayVal) {
                            Lexeme op = new Lexeme(CARET, tree.getLineNumber());
                            op.addChild(lexeme);
                            op.addChild(r);
                            newList.add(evalCaret(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    }
                    case ARRAY -> {
                        for (Lexeme lexeme : l.arrayVal) {
                            Lexeme op = new Lexeme(CARET, tree.getLineNumber());
                            op.addChild(lexeme);
                            op.addChild(new Lexeme(NUMBER, tree.getLineNumber(), r.arrayVal.size()));
                            newList.add(evalCaret(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    }
                    default -> {
                        error("Could not calculate exponent operation", tree);
                        return null;
                    }
                }
            default:
                error("Could not calculate exponent operation", tree);
                return null;
        }
    }

    private Lexeme evalPercent(Lexeme tree, Environment environment) {
        log("evalPercent");
        Lexeme l = eval(tree.getChild(0), environment);
        Lexeme r = eval(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        if (lType == NOTHING_KEYWORD || rType == NOTHING_KEYWORD) {
            error("Could not calculate binary operation with nothing keyword", tree);
            return null;
        }

        switch (lType) {
            case NUMBER:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() % r.getNumVal());
                    case STRING:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() % r.getStringVal().length());
                    case BOOLEAN:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getNumVal() != 0 && r.getBoolVal());
                    case ARRAY:
                        ArrayList<Lexeme> newList = new ArrayList<>();
                        for (Lexeme lexeme : r.arrayVal) {
                            Lexeme op = new Lexeme(PERCENT, tree.getLineNumber());
                            op.addChild(l);
                            op.addChild(lexeme);
                            newList.add(evalPercent(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    default:
                        error("Could not calculate modulus operation", tree);
                        return null;
                }
            case STRING:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal().substring(0, (int) (l.getStringVal().length() % r.getNumVal())));
                    case STRING:
                        return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal().substring(0, (int) (l.getStringVal().length() % r.getStringVal().length())));
                    case BOOLEAN:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), !(l.getStringVal().equals("")) && r.getBoolVal());
                    case ARRAY:
                        ArrayList<Lexeme> newList = new ArrayList<>();
                        for (Lexeme lexeme : r.arrayVal) {
                            Lexeme op = new Lexeme(PERCENT, tree.getLineNumber());
                            op.addChild(l);
                            op.addChild(lexeme);
                            newList.add(evalPercent(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    default:
                        error("Could not calculate modulus operation", tree);
                        return null;
                }
            case BOOLEAN:
                switch (rType) {
                    case NUMBER:
                    case STRING:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal());
                    case BOOLEAN:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getBoolVal() && !r.getBoolVal());
                    case ARRAY:
                        ArrayList<Lexeme> newList = new ArrayList<>();
                        for (Lexeme lexeme : r.arrayVal) {
                            Lexeme op = new Lexeme(PERCENT, tree.getLineNumber());
                            op.addChild(l);
                            op.addChild(lexeme);
                            newList.add(evalPercent(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    default:
                        error("Could not calculate modulus operation", tree);
                        return null;
                }
            case ARRAY:
                ArrayList<Lexeme> newList = new ArrayList<>();
                switch (rType) {
                    case NUMBER, STRING, BOOLEAN -> {
                        for (Lexeme lexeme : l.arrayVal) {
                            Lexeme op = new Lexeme(PERCENT, tree.getLineNumber());
                            op.addChild(lexeme);
                            op.addChild(r);
                            newList.add(evalPercent(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    }
                    case ARRAY -> {
                        for (Lexeme lexeme : l.arrayVal) {
                            Lexeme op = new Lexeme(PERCENT, tree.getLineNumber());
                            op.addChild(lexeme);
                            op.addChild(new Lexeme(NUMBER, tree.getLineNumber(), r.arrayVal.size()));
                            newList.add(evalPercent(op, environment));
                        }
                        return new Lexeme(ARRAY, tree.getLineNumber(), newList);
                    }
                    default -> {
                        error("Could not calculate modulus operation", tree);
                        return null;
                    }
                }
            default:
                error("Could not calculate modulus operation", tree);
                return null;
        }
    }

    private Lexeme evalCast(Lexeme tree, Environment environment) {
        Lexeme value = eval(tree.getChild(1), environment);
        Lexeme type = tree.getChild(0);
        switch (value.getType()) {
            case NUMBER:
                switch (type.getType()) {
                    case NUM_KEYWORD:
                        return value;
                    case STR_KEYWORD:
                        return new Lexeme(STRING, tree.getLineNumber(), value.getNumVal().toString());
                    case TF_KEYWORD:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), value.getNumVal() != 0);
                    case ARR_KEYWORD:
                        ArrayList<Lexeme> temp = new ArrayList<>();
                        temp.add(value);
                        return new Lexeme(ARRAY, tree.getLineNumber(), temp);
                    default:
                        error("Could not perform cast", tree);
                        return null;
                }
            case STRING:
                switch (type.getType()) {
                    case NUM_KEYWORD:
                        return new Lexeme(NUMBER, tree.getLineNumber(), value.getStringVal().length());
                    case STR_KEYWORD:
                        return value;
                    case TF_KEYWORD:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), !value.getStringVal().equals(""));
                    case ARR_KEYWORD:
                        ArrayList<Lexeme> temp = new ArrayList<>();
                        temp.add(value);
                        return new Lexeme(ARRAY, tree.getLineNumber(), temp);
                    default:
                        error("Could not perform cast", tree);
                        return null;
                }
            case BOOLEAN:
                switch (type.getType()) {
                    case NUM_KEYWORD:
                        return new Lexeme(NUMBER, tree.getLineNumber(), value.getBoolVal() ? 1 : 0);
                    case STR_KEYWORD:
                        return new Lexeme(STRING, tree.getLineNumber(), value.getBoolVal() ? "true" : "fals");
                    case TF_KEYWORD:
                        return value;
                    case ARR_KEYWORD:
                        ArrayList<Lexeme> temp = new ArrayList<>();
                        temp.add(value);
                        return new Lexeme(ARRAY, tree.getLineNumber(), temp);
                    default:
                        error("Could not perform cast", tree);
                        return null;
                }
            case ARRAY:
                switch (type.getType()) {
                    case NUM_KEYWORD:
                        return new Lexeme(NUMBER, tree.getLineNumber(), value.arrayVal.size());
                    case STR_KEYWORD:
                        StringBuilder temp = new StringBuilder("(");
                        for (Lexeme lexeme : value.arrayVal) {
                            Lexeme cast = new Lexeme(CAST, tree.getLineNumber());
                            cast.addChild(new Lexeme(STR_KEYWORD, tree.getLineNumber()));
                            cast.addChild(lexeme);
                            Lexeme castedString = evalCast(cast, environment);
                            if (castedString != null) {
                                temp.append(castedString.getStringVal());
                                temp.append(" ");
                            }
                        }
                        if (temp.length() > 1) temp = new StringBuilder(temp.substring(0, temp.length() - 1));
                        temp.append(")");
                        return new Lexeme(STRING, tree.getLineNumber(), temp.toString());
                    case TF_KEYWORD:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), value.arrayVal.size() != 0);
                    case ARR_KEYWORD:
                        return value;
                    default:
                        error("Could not perform cast", tree);
                        return null;
                }
            case NOTHING_KEYWORD:
                error("Could not perform cast with nothing keyword", tree);
                return null;
            default:
                error("Could not perform cast", tree);
                return null;
        }
    }

    private boolean isTruthy(Lexeme lexeme) {
        if (lexeme.getType() == NOTHING_KEYWORD) return false;
        return ((lexeme.getNumVal() == null ? 1 : lexeme.getNumVal()) != 0) // if non null, not 0
                && !((lexeme.getStringVal() == null ? " " : lexeme.getStringVal()).equals("")) // if non null, not empty
                && (lexeme.getBoolVal() == null || lexeme.getBoolVal()); // if non null, not false
    }

    private static void log(String message) {
        if (printDebugMessages) System.out.println(message);
    }

    private void error(String message, int lineNumber) {
        Sigma.runtimeError(message, lineNumber);
    }

    private void error(String message, Lexeme lexeme) {
        Sigma.runtimeError(message, lexeme);
    }
}