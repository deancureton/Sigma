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
            case PROGRAM -> eval(tree.getChild(0), environment);
            case STATEMENT_LIST -> evalStatementList(tree, environment);

            case EXPRESSION -> eval(tree.getChild(0), environment);

            case VARIABLE_DECLARATION -> evalVariableDeclaration(tree, environment);
            case FUNCTION_DEFINITION -> evalFunctionDefinition(tree, environment);

            case IF_STATEMENT -> evalIfStatement(tree, environment);

            case CHANGE_STATEMENT -> evalChangeStatement(tree, environment);

            case FOR_LOOP -> evalForLoop(tree, environment);
            case FOREACH_LOOP -> evalForeachLoop(tree, environment);
            case WHEN_LOOP -> evalWhenLoop(tree, environment);
            case LOOP_LOOP -> evalLoopLoop(tree, environment);

            case ASSIGNMENT -> evalAssignment(tree, environment);

            case PLUS, MINUS, TIMES, DIVIDE, DOUBLE_DIVIDE, CARET, PERCENT -> evalSimpleBinaryOperator(tree, environment);
            case QUESTION, APPROX, GREATER, LESS, GEQ, LEQ,
                    NOT_QUESTION, NOT_APPROX,
                    DOUBLE_QUESTION, GREATER_QUESTION, LESS_QUESTION,
                    NOT_DOUBLE_QUESTION -> evalBinaryComparator(tree, environment);
            case AND_KEYWORD, OR_KEYWORD,
                    NAND_KEYWORD, NOR_KEYWORD, XOR_KEYWORD, XNOR_KEYWORD -> evalBooleanBinaryOperator(tree, environment);

            case NUMBER, STRING, BOOLEAN, COMMENT -> tree;
            case IDENTIFIER -> environment.lookup(tree);
            case CAST -> evalCast(tree, environment);

            default -> null;
        };
    }

    private Lexeme evalStatementList(Lexeme tree, Environment environment) {
        log("evalStatementList");
        Lexeme result = null;
        for (int i = 0; i < tree.getNumChildren(); i++) {
            result = eval(tree.getChild(i), environment);
        }
        return result;
    }

    private Lexeme evalVariableDeclaration(Lexeme tree, Environment environment) {
        environment.add(tree.getChild(0), tree.getChild(1));
        return tree.getChild(1);
    }

    private Lexeme evalFunctionDefinition(Lexeme tree, Environment environment) {
        return null;
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
                return result;
            }
            case DECREMENT -> {
                Lexeme minus = new Lexeme(MINUS, tree.getLineNumber());
                minus.addChild(id);
                minus.addChild(one);
                Lexeme result = evalMinus(minus, environment);
                environment.update(id, result);
                return result;
            }
            case REGULAR_ASSIGNMENT -> {
                return evalRegularAssignment(tree, environment);
            }
            default -> {
                error("Problem with variable assignment", tree);
                return null;
            }
        }
    }

    private Lexeme evalRegularAssignment(Lexeme tree, Environment environment) {
        Lexeme id = tree.getChild(0);
        Lexeme op = tree.getChild(1).getChild(0);
        Lexeme exp = tree.getChild(1).getChild(1);
        switch (op.getType()) {
            case ASSIGN_OPERATOR -> {
                Lexeme result = eval(exp, environment);
                environment.update(id, result);
                return result;
            }
            case PLUS_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(PLUS, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalPlus(newTree, environment);
                environment.update(id, result);
                return result;
            }
            case MINUS_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(MINUS, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalMinus(newTree, environment);
                environment.update(id, result);
                return result;
            }
            case TIMES_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(TIMES, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalTimes(newTree, environment);
                environment.update(id, result);
                return result;
            }
            case DIVIDE_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(DIVIDE, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalDivide(newTree, environment);
                environment.update(id, result);
                return result;
            }
            case DOUBLE_DIVIDE_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(DOUBLE_DIVIDE, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalDoubleDivide(newTree, environment);
                environment.update(id, result);
                return result;
            }
            case CARET_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(CARET, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalCaret(newTree, environment);
                environment.update(id, result);
                return result;
            }
            case PERCENT_ASSIGNMENT -> {
                Lexeme newTree = new Lexeme(PERCENT, tree.getLineNumber());
                newTree.addChild(id);
                newTree.addChild(exp);
                Lexeme result = evalPercent(newTree, environment);
                environment.update(id, result);
                return result;
            }
            default -> {
                error("Problem with assignment operator", tree);
                return null;
            }
        }
    }

    private Lexeme evalIfStatement(Lexeme tree, Environment environment) {
        return null;
    }

    private Lexeme evalChangeStatement(Lexeme tree, Environment environment) {
        return null;
    }

    private Lexeme evalForLoop(Lexeme tree, Environment environment) {
        return null;
    }

    private Lexeme evalForeachLoop(Lexeme tree, Environment environment) {
        return null;
    }

    private Lexeme evalWhenLoop(Lexeme tree, Environment environment) {
        return null;
    }

    private Lexeme evalLoopLoop(Lexeme tree, Environment environment) {
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
        return null;
    }

    private Lexeme evalBooleanBinaryOperator(Lexeme tree, Environment environment) {
        boolean l = ((tree.getChild(0).getNumVal() == null ? 1 : tree.getChild(0).getNumVal()) != 0)
                && !((tree.getChild(0).getStringVal() == null ? " " : tree.getChild(0).getStringVal()).equals(""))
                && (tree.getChild(0).getBoolVal() == null || tree.getChild(0).getBoolVal());
        boolean r = ((tree.getChild(1).getNumVal() == null ? 1 : tree.getChild(1).getNumVal()) != 0)
                && !((tree.getChild(1).getStringVal() == null ? " " : tree.getChild(1).getStringVal()).equals(""))
                && (tree.getChild(1).getBoolVal() == null || tree.getChild(1).getBoolVal());
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
                    case BOOLEAN:
                        return new Lexeme(STRING, tree.getLineNumber(), r.getBoolVal() ? l.getStringVal() : "");
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

        switch (lType) {
            case NUMBER:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() / r.getNumVal());
                    case STRING:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() / r.getStringVal().length());
                    case BOOLEAN:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() / (r.getBoolVal() ? 1 : 0));
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

        switch (lType) {
            case NUMBER:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), Math.floor(l.getNumVal() / r.getNumVal()));
                    case STRING:
                        return new Lexeme(NUMBER, tree.getLineNumber(), Math.floor(l.getNumVal() / r.getStringVal().length()));
                    case BOOLEAN:
                        return new Lexeme(NUMBER, tree.getLineNumber(), Math.floor(l.getNumVal() / (r.getBoolVal() ? 1 : 0)));
                    default:
                        error("Could not calculate double divide operation", tree);
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
                    default:
                        error("Could not calculate double divide operation", tree);
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
                    default:
                        error("Could not calculate double divide operation", tree);
                        return null;
                }
            default:
                error("Could not calculate double divide operation", tree);
                return null;
        }
    }

    private Lexeme evalCaret(Lexeme tree, Environment environment) {
        log("evalCaret");
        Lexeme l = eval(tree.getChild(0), environment);
        Lexeme r = eval(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        switch (lType) {
            case NUMBER:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), Math.pow(l.getNumVal(), r.getNumVal()));
                    case STRING:
                        return new Lexeme(NUMBER, tree.getLineNumber(), Math.pow(l.getNumVal(), r.getStringVal().length()));
                    case BOOLEAN:
                        return new Lexeme(NUMBER, tree.getLineNumber(), Math.pow(l.getNumVal(), (r.getBoolVal() ? 1 : 0)));
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
                    default:
                        error("Could not calculate exponent operation", tree);
                        return null;
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

        switch (lType) {
            case NUMBER:
                switch (rType) {
                    case NUMBER:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() % r.getNumVal());
                    case STRING:
                        return new Lexeme(NUMBER, tree.getLineNumber(), l.getNumVal() % r.getStringVal().length());
                    case BOOLEAN:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), l.getNumVal() != 0 && r.getBoolVal());
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
                    default:
                        error("Could not calculate modulus operation", tree);
                        return null;
                }
            default:
                error("Could not calculate modulus operation", tree);
                return null;
        }
    }

    private Lexeme evalCast(Lexeme tree, Environment environment) {
        return null;
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