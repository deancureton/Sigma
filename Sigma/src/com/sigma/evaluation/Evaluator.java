package com.sigma.evaluation;

import com.sigma.Sigma;
import com.sigma.environments.Environment;
import com.sigma.lexicalAnalysis.Lexeme;
import com.sigma.lexicalAnalysis.TokenType;

import java.util.Arrays;

import static com.sigma.lexicalAnalysis.TokenType.*;

// TODO how will there be multiple outputs if only one lexeme is returned
// TODO take (return), end (break), fall (continue), count (loop count)
// TODO ¬ as tab and ˇ as newline in strings

public class Evaluator {
    private static final boolean printDebugMessages = false;
    public Lexeme eval(Lexeme tree, Environment environment) {
        if (tree == null) return null;

        return switch (tree.getType()) {
            case NUMBER, STRING, BOOLEAN, COMMENT -> tree;

            case IDENTIFIER -> environment.lookup(tree);

            case PLUS, MINUS, TIMES, DIVIDE, DOUBLE_DIVIDE,
                    CARET, PERCENT,
                    QUESTION, APPROX, GREATER, LESS, GEQ, LEQ,
                    NOT_QUESTION, NOT_APPROX,
                    DOUBLE_QUESTION, GREATER_QUESTION, LESS_QUESTION,
                    NOT_DOUBLE_QUESTION,
                    PLUS_ASSIGNMENT, MINUS_ASSIGNMENT, TIMES_ASSIGNMENT, DIVIDE_ASSIGNMENT, DOUBLE_DIVIDE_ASSIGNMENT,
                    CARET_ASSIGNMENT, PERCENT_ASSIGNMENT -> evalBinaryOperator(tree, environment);

            case PROGRAM -> eval(tree.getChild(0), environment);
            case STATEMENT_LIST -> evalStatementList(tree, environment);

            default -> null;
        };
    }

    private Lexeme evalStatementList(Lexeme statementList, Environment environment) {
        log("evalStatementList");
        Lexeme result = null;
        for (int i = 0; i < statementList.getNumChildren(); i++) {
            result = eval(statementList.getChild(i), environment);
        }
        return result;
    }

    private Lexeme evalBinaryOperator(Lexeme tree, Environment environment) {
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
            default -> {
                error("Unrecognized operator: " + tree.getType(), tree);
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

        switch (lType) {
            case NUMBER:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() + r.getNumVal());
                    case STRING:
                        return new Lexeme(STRING, tree.getLineNumber(), l.getNumVal() + r.getStringVal());
                    case BOOLEAN:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() + (r.getBoolVal() ? 1 : 0));
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
                    default:
                        error("Could not calculate plus operation", tree);
                        return null;
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
                default:
                    error("Invalid type after unary minus operator", tree);
                    return null;
            }
        } else if (tree.getNumChildren() == 2) {
            Lexeme l = eval(tree.getChild(0), environment);
            Lexeme r = eval(tree.getChild(1), environment);
            TokenType lType = l.getType();
            TokenType rType = r.getType();

            switch (lType) {
                case NUMBER:
                    switch (rType) {
                        case NUMBER:
                            return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() - r.getNumVal());
                        case STRING:
                            return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() - r.getStringVal().length());
                        case BOOLEAN:
                            return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() - (r.getBoolVal() ? 1 : 0));
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
                        default:
                            error("Could not calculate minus operation", tree);
                            return null;
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

    private Lexeme evalTimes(Lexeme tree, Environment environment) {
        log("evalTimes");
        Lexeme l = eval(tree.getChild(0), environment);
        Lexeme r = eval(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        switch (lType) {
            case NUMBER:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() * r.getNumVal());
                    case STRING:
                        return new Lexeme(STRING, tree.getLineNumber(), r.getStringVal().repeat((int) Math.floor(l.getNumVal())) + r.getStringVal().substring(0, (int) (r.getStringVal().length() * (l.getNumVal() % 1))));
                    case BOOLEAN:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), r.getBoolVal());
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
                    case BOOLEAN: // TODO and everything below
                        return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal() + (r.getBoolVal() ? "true" : "fals"));
                    default:
                        error("Could not calculate times operation", tree);
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
                    default:
                        error("Could not calculate times operation", tree);
                        return null;
                }
            default:
                error("Could not calculate times operation", tree);
                return null;
        }
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