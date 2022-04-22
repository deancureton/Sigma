package com.sigma.evaluation;

import com.sigma.Sigma;
import com.sigma.environments.Environment;
import com.sigma.lexicalAnalysis.Lexeme;
import com.sigma.lexicalAnalysis.TokenType;

import java.util.*;

import static com.sigma.lexicalAnalysis.TokenType.*;

public class Evaluator {
    private static final boolean printDebugMessages = false;

    public Lexeme eval(Lexeme tree, Environment environment) {
        log("eval");
        if (tree == null) return null;

        return switch (tree.getType()) {
            case PROGRAM -> eval(tree.getChild(0), environment);
            case STATEMENT_LIST -> evalStatementList(tree, environment);
            default -> evalStatement(tree, environment);
        };
    }

    private Lexeme evalStatementList(Lexeme tree, Environment environment) {
        log("evalStatementList");
        Lexeme result = null;
        for (int i = 0; i < tree.getNumChildren(); i++) {
            result = evalStatement(tree.getChild(i), environment);
        }
        return result;
    }

    private Lexeme evalStatement(Lexeme tree, Environment environment) {
        //log("evalStatement");
        if (tree == null) return null;

        return switch (tree.getType()) {
            case EXPRESSION -> evalStatement(tree.getChild(0), environment);

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

            case NUMBER, STRING, BOOLEAN, NOTHING -> tree;
            case ARRAY -> evalArray(tree, environment);
            case IDENTIFIER -> environment.lookup(tree);

            default -> null;
        };
    }

    private Lexeme evalVariableDeclaration(Lexeme tree, Environment environment) {
        log("evalVariableDeclaration");
        if (tree.getNumChildren() == 2) {
            Lexeme result = evalStatement(tree.getChild(1), environment);
            environment.add(tree.getChild(0), result);
        } else if (tree.getNumChildren() == 1) {
            environment.add(tree.getChild(0), new Lexeme(NOTHING, tree.getLineNumber()));
        } else {
            Sigma.runtimeError("Invalid variable declaration", tree);
        }
        return null;
    }

    private Lexeme evalFunctionDefinition(Lexeme tree, Environment environment) {
        log("evalFunctionDefinition");
        tree.setDefiningEnvironment(environment);
        switch (tree.getChild(0).getStringVal()) {
            case "log", "random", "abs", "floor", "ceil", "round", "sqrt", "min", "max", "lowercase", "uppercase", "getChar", "substring", "length", "get", "set", "add", "remove", "contains", "str", "num", "tf", "arr" -> Sigma.runtimeError("Cannot override built-in function " + tree.getChild(0).getStringVal(), tree);
            default -> environment.add(tree.getChild(0), tree);
        }
        return null;
    }

    private Lexeme evalFunctionCall(Lexeme tree, Environment environment) {
        log("evalFunctionCall");
        Lexeme functionName = tree.getChild(0);
        switch (functionName.getStringVal()) {
            case "log" -> {
                for (int i = 0; i < tree.getChild(1).getNumChildren(); i++) {
                    System.out.println(evalStatement(tree.getChild(1).getChild(i), environment));
                }
                if (tree.getChild(1).getNumChildren() == 0) System.out.println();
                return null;
            }
            case "random" -> {
                if (tree.getChild(1).getNumChildren() != 2) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                Lexeme arg2 = evalStatement(tree.getChild(1).getChild(1), environment);
                if (arg1.getType() != NUMBER || arg2.getType() != NUMBER) {
                    Sigma.runtimeError("random takes in two number arguments", tree.getChild(1));
                    return null;
                }
                Random rand = new Random();
                double random = rand.nextDouble();
                random = random * (arg2.getNumVal() - arg1.getNumVal()) + arg1.getNumVal();
                return new Lexeme(NUMBER, tree.getLineNumber(), random);
            }
            case "abs" -> {
                if (tree.getChild(1).getNumChildren() != 1) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg = evalStatement(tree.getChild(1).getChild(0), environment);
                if (arg.getType() != NUMBER) {
                    Sigma.runtimeError("abs takes in one number argument", tree.getChild(1));
                    return null;
                }
                return new Lexeme(NUMBER, tree.getLineNumber(), Math.abs(arg.getNumVal()));
            }
            case "floor" -> {
                if (tree.getChild(1).getNumChildren() != 1) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg = evalStatement(tree.getChild(1).getChild(0), environment);
                if (arg.getType() != NUMBER) {
                    Sigma.runtimeError("floor takes in one number argument", tree.getChild(1));
                    return null;
                }
                return new Lexeme(NUMBER, tree.getLineNumber(), Math.floor(arg.getNumVal()));
            }
            case "ceil" -> {
                if (tree.getChild(1).getNumChildren() != 1) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg = evalStatement(tree.getChild(1).getChild(0), environment);
                if (arg.getType() != NUMBER) {
                    Sigma.runtimeError("ceil takes in one number argument", tree.getChild(1));
                    return null;
                }
                return new Lexeme(NUMBER, tree.getLineNumber(), Math.ceil(arg.getNumVal()));
            }
            case "round" -> {
                if (tree.getChild(1).getNumChildren() != 1) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg = evalStatement(tree.getChild(1).getChild(0), environment);
                if (arg.getType() != NUMBER) {
                    Sigma.runtimeError("round takes in one number argument", tree.getChild(1));
                    return null;
                }
                return new Lexeme(NUMBER, tree.getLineNumber(), Math.round(arg.getNumVal()));
            }
            case "sqrt" -> {
                if (tree.getChild(1).getNumChildren() != 1) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg = evalStatement(tree.getChild(1).getChild(0), environment);
                if (arg.getType() != NUMBER) {
                    Sigma.runtimeError("sqrt takes in one number argument", tree.getChild(1));
                    return null;
                }
                if (arg.getNumVal() < 0) {
                    Sigma.runtimeError("Cannot take square root of negative number", tree.getChild(1));
                    return null;
                }
                return new Lexeme(NUMBER, tree.getLineNumber(), Math.sqrt(arg.getNumVal()));
            }
            case "min" -> {
                if (tree.getChild(1).getNumChildren() != 2) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                Lexeme arg2 = evalStatement(tree.getChild(1).getChild(1), environment);
                if (arg1.getType() != NUMBER || arg2.getType() != NUMBER) {
                    Sigma.runtimeError("min takes in two number arguments", tree.getChild(1));
                    return null;
                }
                return new Lexeme(NUMBER, tree.getLineNumber(), Math.min(arg1.getNumVal(), arg2.getNumVal()));
            }
            case "max" -> {
                if (tree.getChild(1).getNumChildren() != 2) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                Lexeme arg2 = evalStatement(tree.getChild(1).getChild(1), environment);
                if (arg1.getType() != NUMBER || arg2.getType() != NUMBER) {
                    Sigma.runtimeError("max takes in two number arguments", tree.getChild(1));
                    return null;
                }
                return new Lexeme(NUMBER, tree.getLineNumber(), Math.max(arg1.getNumVal(), arg2.getNumVal()));
            }
            case "lowercase" -> {
                if (tree.getChild(1).getNumChildren() != 1) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                if (arg1.getType() != STRING) {
                    Sigma.runtimeError("lowercase takes in one string argument", tree.getChild(1));
                    return null;
                }
                return new Lexeme(STRING, tree.getLineNumber(), arg1.getStringVal().toLowerCase());
            }
            case "uppercase" -> {
                if (tree.getChild(1).getNumChildren() != 1) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                if (arg1.getType() != STRING) {
                    Sigma.runtimeError("uppercase takes in one string argument", tree.getChild(1));
                    return null;
                }
                return new Lexeme(STRING, tree.getLineNumber(), arg1.getStringVal().toUpperCase());
            }
            case "getchar" -> {
                if (tree.getChild(1).getNumChildren() != 2) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                Lexeme arg2 = evalStatement(tree.getChild(1).getChild(1), environment);
                if (arg1.getType() != STRING || arg2.getType() != NUMBER) {
                    Sigma.runtimeError("getchar takes in one string and one number arguments", tree.getChild(1));
                    return null;
                }
                String arg1str = arg1.getStringVal();
                int index = (int) Math.floor(arg2.getNumVal());
                if (index > arg1str.length() - 1) {
                    Sigma.runtimeError("getchar string index out of range", tree.getChild(1));
                    return null;
                }
                return new Lexeme(STRING, tree.getLineNumber(), Character.toString(arg1str.charAt(index)));
            }
            case "substring" -> {
                if (tree.getChild(1).getNumChildren() != 3) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                Lexeme arg2 = evalStatement(tree.getChild(1).getChild(1), environment);
                Lexeme arg3 = evalStatement(tree.getChild(1).getChild(2), environment);
                if (arg1.getType() != STRING || arg2.getType() != NUMBER || arg3.getType() != NUMBER) {
                    Sigma.runtimeError("substring takes in one string and two number arguments", tree.getChild(1));
                    return null;
                }
                return new Lexeme(STRING, tree.getLineNumber(), arg1.getStringVal().substring((int) Math.floor(arg2.getNumVal()), (int) Math.floor(arg3.getNumVal())));
            }
            case "length" -> {
                if (tree.getChild(1).getNumChildren() != 1) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                if (arg1.getType() == STRING) {
                    return new Lexeme(NUMBER, tree.getLineNumber(), arg1.getStringVal().length());
                } else if (arg1.getType() == ARRAY) {
                    return new Lexeme(NUMBER, tree.getLineNumber(), arg1.arrayVal.size());
                } else {
                    Sigma.runtimeError("length takes in one string or array argument", tree.getChild(1));
                    return null;
                }
            }
            case "get" -> {
                if (tree.getChild(1).getNumChildren() != 2) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                Lexeme arg2 = evalStatement(tree.getChild(1).getChild(1), environment);
                if (arg1.getType() != ARRAY || arg2.getType() != NUMBER) {
                    Sigma.runtimeError("get takes in one array and one number arguments", tree.getChild(1));
                    return null;
                }
                int index = (int) Math.floor(arg2.getNumVal());
                if (index < 0 || index >= arg1.arrayVal.size()) {
                    Sigma.runtimeError("get index out of bounds", tree.getChild(1));
                    return null;
                }
                return arg1.arrayVal.get((int) Math.floor(arg2.getNumVal()));
            }
            case "set" -> {
                if (tree.getChild(1).getNumChildren() != 3) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                Lexeme arg2 = evalStatement(tree.getChild(1).getChild(1), environment);
                Lexeme arg3 = evalStatement(tree.getChild(1).getChild(2), environment);
                if (arg1.getType() != ARRAY || arg3.getType() != NUMBER) {
                    Sigma.runtimeError("add takes in one array, one anytype, and one number arguments", tree.getChild(1));
                    return null;
                }
                int index = (int) Math.floor(arg3.getNumVal());
                if (index < 0 || index >= arg1.arrayVal.size()) {
                    Sigma.runtimeError("set index out of bounds", tree.getChild(1));
                    return null;
                }
                arg1.arrayVal.set(index, arg2);
                return null;
            }
            case "add" -> {
                if (tree.getChild(1).getNumChildren() != 3) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                Lexeme arg2 = evalStatement(tree.getChild(1).getChild(1), environment);
                Lexeme arg3 = evalStatement(tree.getChild(1).getChild(2), environment);
                if (arg1.getType() != ARRAY || arg3.getType() != NUMBER) {
                    Sigma.runtimeError("add takes in one array, one anytype, and one number arguments", tree.getChild(1));
                    return null;
                }
                int index = (int) Math.floor(arg3.getNumVal());
                if (index < 0 || index > arg1.arrayVal.size()) {
                    Sigma.runtimeError("add index out of bounds", tree.getChild(1));
                    return null;
                }
                arg1.arrayVal.add(index, arg2);
                return null;
            }
            case "remove" -> {
                if (tree.getChild(1).getNumChildren() != 2) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                Lexeme arg2 = evalStatement(tree.getChild(1).getChild(1), environment);
                if (arg1.getType() != ARRAY || arg2.getType() != NUMBER) {
                    Sigma.runtimeError("remove takes in one array and one number arguments", tree.getChild(1));
                    return null;
                }
                int index = (int) Math.floor(arg2.getNumVal());
                if (index < 0 || index >= arg1.arrayVal.size()) {
                    Sigma.runtimeError("remove index out of bounds", tree.getChild(1));
                    return null;
                }
                arg1.arrayVal.remove(index);
                return null;
            }
            case "contains" -> {
                if (tree.getChild(1).getNumChildren() != 2) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                Lexeme arg2 = evalStatement(tree.getChild(1).getChild(1), environment);
                if (arg1.getType() != ARRAY) {
                    Sigma.runtimeError("remove takes in one array and one number arguments", tree.getChild(1));
                    return null;
                }
                for (Lexeme lexeme : arg1.arrayVal) {
                    if (lexeme.equals(arg2)) {
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), true);
                    }
                }
                return new Lexeme(BOOLEAN, tree.getLineNumber(), false);
            }
            case "num" -> {
                if (tree.getChild(1).getNumChildren() != 1) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                switch (arg1.getType()) {
                    case NUMBER:
                        return arg1;
                    case STRING:
                        return new Lexeme(NUMBER, tree.getLineNumber(), arg1.getStringVal().length());
                    case BOOLEAN:
                        return new Lexeme(NUMBER, tree.getLineNumber(), arg1.getBoolVal() ? 1 : 0);
                    case ARRAY:
                        return new Lexeme(NUMBER, tree.getLineNumber(), arg1.arrayVal.size());
                    default:
                        error("Could not perform cast", tree);
                        return null;
                }
            }
            case "str" -> {
                if (tree.getChild(1).getNumChildren() != 1) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                switch (arg1.getType()) {
                    case NUMBER:
                        return new Lexeme(STRING, tree.getLineNumber(), arg1.getNumVal().toString());
                    case STRING:
                        return arg1;
                    case BOOLEAN:
                        return new Lexeme(STRING, tree.getLineNumber(), arg1.getBoolVal() ? "true" : "fals");
                    case ARRAY:
                        StringBuilder temp = new StringBuilder("(");
                        for (Lexeme lexeme : arg1.arrayVal) {
                            Lexeme str = new Lexeme(FUNCTION_CALL, tree.getLineNumber());
                            str.addChild(new Lexeme(IDENTIFIER, tree.getLineNumber(), "str"));
                            Lexeme arguments = new Lexeme(CALL_ARGUMENTS, tree.getLineNumber());
                            arguments.addChild(lexeme);
                            str.addChild(arguments);
                            Lexeme castedString = evalFunctionCall(str, environment);
                            if (castedString != null) {
                                temp.append(castedString.getStringVal());
                                temp.append(" ");
                            }
                        }
                        if (temp.length() > 1) temp = new StringBuilder(temp.substring(0, temp.length() - 1));
                        temp.append(")");
                        return new Lexeme(STRING, tree.getLineNumber(), temp.toString());
                    default:
                        error("Could not perform cast", tree);
                        return null;
                }
            }
            case "tf" -> {
                if (tree.getChild(1).getNumChildren() != 1) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                switch (arg1.getType()) {
                    case NUMBER:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), arg1.getNumVal() != 0);
                    case STRING:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), !arg1.getStringVal().equals(""));
                    case BOOLEAN:
                        return arg1;
                    case ARRAY:
                        return new Lexeme(BOOLEAN, tree.getLineNumber(), arg1.arrayVal.size() != 0);
                    default:
                        error("Could not perform cast", tree);
                        return null;
                }
            }
            case "arr" -> {
                if (tree.getChild(1).getNumChildren() != 1) {
                    Sigma.runtimeError("Invalid number of function arguments", tree.getChild(1));
                    return null;
                }
                Lexeme arg1 = evalStatement(tree.getChild(1).getChild(0), environment);
                ArrayList<Lexeme> temp = new ArrayList<>();
                switch (arg1.getType()) {
                    case NUMBER, STRING, BOOLEAN:
                        temp.add(arg1);
                        return new Lexeme(ARRAY, tree.getLineNumber(), temp);
                    case ARRAY:
                        return arg1;
                    default:
                        error("Could not perform cast", tree);
                        return null;
                }
            }
            default -> {
                Lexeme closure = environment.lookup(functionName);
                if (closure.getType() != FUNCTION_DEFINITION)
                    error("Attempt to call " + closure.getType() + " as function failed", functionName);
                Environment definingEnv = closure.getDefiningEnvironment();
                Environment funcEnv = new Environment(definingEnv);
                Environment callEnv = new Environment(funcEnv);
                Lexeme paramList = closure.getChild(1);
                Lexeme argList = tree.getChild(1);
                Lexeme evalArgList = evalArgumentList(argList, environment);
                funcEnv.extend(paramList, evalArgList);
                Lexeme functionBody = closure.getChild(2);
                return evalStatementList(functionBody, callEnv);
            }
        }
    }

    private Lexeme evalArgumentList(Lexeme tree, Environment environment) {
        log("evalArgumentList");
        Lexeme evaluated = new Lexeme(CALL_ARGUMENTS, tree.getLineNumber());
        for (int i = 0; i < tree.getNumChildren(); i++) {
            evaluated.addChild(evalStatement(tree.getChild(i), environment));
        }
        return evaluated;
    }

    private Lexeme evalAssignment(Lexeme tree, Environment environment) {
        log("evalAssignment");
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
        log("evalRegularAssignment");
        Lexeme id = tree.getChild(0);
        Lexeme op = tree.getChild(1).getChild(0);
        Lexeme exp = tree.getChild(1).getChild(1);
        switch (op.getType()) {
            case ASSIGN_OPERATOR -> {
                Lexeme result = evalStatement(exp, environment);
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
        log("evalIfStatement");
        Lexeme ifExp = evalStatement(tree.getChild(0), environment);
        if (isTruthy(ifExp)) {
            Environment ifEnv = new Environment(environment);
            return evalStatementList(tree.getChild(1), ifEnv);
        }
        for (int i = 0; i < tree.getChild(2).getNumChildren(); i++) {
            Lexeme butIfExp = evalStatement(tree.getChild(2).getChild(i).getChild(0), environment);
            if (isTruthy(butIfExp)) {
                Environment butIfEnv = new Environment(environment);
                return evalStatementList(tree.getChild(2).getChild(i).getChild(1), butIfEnv);
            }
        }
        if (tree.getNumChildren() == 4) {
            Environment butEnv = new Environment(environment);
            return evalStatementList(tree.getChild(3).getChild(0), butEnv);
        }
        return null;
    }

    private Lexeme evalChangeStatement(Lexeme tree, Environment environment) {
        log("evalChangeStatement");
        if (tree.getChild(0) == null) {
            error("Missing identifier", tree);
            return null;
        }
        Lexeme value = evalStatement(tree.getChild(0), environment);
        boolean caught = false;
        for (int i = 0; i < tree.getChild(1).getNumChildren() - 1; i++) {
            Lexeme op = new Lexeme(QUESTION, tree.getLineNumber());
            op.addChild(value);
            if (evalStatement(tree.getChild(1).getChild(i).getChild(0), environment) == null) {
                error("Missing expression", tree.getChild(1).getChild(i));
                return null;
            } else {
                op.addChild(evalStatement(tree.getChild(1).getChild(i).getChild(0), environment));
                Lexeme evaluated = evalBinaryComparator(op, environment);
                if (evaluated == null) {
                    error("Error calculating change statement", tree.getChild(1).getChild(i).getChild(0));
                    return null;
                } else if (isTruthy(evaluated)) {
                    Environment caseEnvironment = new Environment(environment);
                    evalStatementList(tree.getChild(1).getChild(i).getChild(1), caseEnvironment);
                    caught = true;
                    break;
                }
            }
        }
        if (!caught) {
            Environment caseEnvironment = new Environment(environment);
            evalStatementList(tree.getChild(1).getChild(tree.getChild(1).getNumChildren() - 1).getChild(0), caseEnvironment);
        }
        return null;
    }

    private Lexeme evalForLoop(Lexeme tree, Environment environment) {
        log("evalForLoop");
        Environment forEnvironment = new Environment(environment);
        forEnvironment.add(tree.getChild(0).getChild(0), evalStatement(tree.getChild(0).getChild(1), forEnvironment));
        Lexeme count = new Lexeme(IDENTIFIER, tree.getLineNumber(), "count");
        forEnvironment.add(count, new Lexeme(NUMBER, tree.getLineNumber(), 0));
        while (isTruthy(evalStatement(tree.getChild(1), forEnvironment))) {
            Environment forBody = new Environment(forEnvironment);
            evalStatementList(tree.getChild(3), forBody);
            forEnvironment.update(count, new Lexeme(NUMBER, tree.getLineNumber(), forEnvironment.lookup(count).getNumVal() + 1));
            evalStatement(tree.getChild(2), forEnvironment);
        }
        return null;
    }

    private Lexeme evalForeachLoop(Lexeme tree, Environment environment) {
        log("evalForeachLoop");
        ArrayList<Lexeme> foreachArray;
        foreachArray = evalStatement(tree.getChild(1), environment).arrayVal;
        Environment foreachEnvironment = new Environment(environment);
        Lexeme count = new Lexeme(IDENTIFIER, tree.getLineNumber(), "count");
        foreachEnvironment.add(count, new Lexeme(NUMBER, tree.getLineNumber(), 0));
        for (Lexeme lexeme : foreachArray) {
            Environment foreachBody = new Environment(foreachEnvironment);
            foreachBody.add(tree.getChild(0), lexeme);
            evalStatementList(tree.getChild(2), foreachBody);
            foreachEnvironment.update(count, new Lexeme(NUMBER, tree.getLineNumber(), foreachEnvironment.lookup(count).getNumVal() + 1));
        }
        return null;
    }

    private Lexeme evalWhenLoop(Lexeme tree, Environment environment) {
        log("evalWhenLoop");
        Environment whenEnvironment = new Environment(environment);
        Lexeme count = new Lexeme(IDENTIFIER, tree.getLineNumber(), "count");
        whenEnvironment.add(count, new Lexeme(NUMBER, tree.getLineNumber(), 0));
        while (isTruthy(evalStatement(tree.getChild(0), environment))) {
            Environment whenBody = new Environment(whenEnvironment);
            evalStatementList(tree.getChild(1), whenBody);
            whenEnvironment.update(count, new Lexeme(NUMBER, tree.getLineNumber(), whenEnvironment.lookup(count).getNumVal() + 1));
        }
        return null;
    }

    private Lexeme evalLoopLoop(Lexeme tree, Environment environment) {
        log("evalLoopLoop");
        if (evalStatement(tree.getChild(0), environment) == null) {
            error("Missing expression", tree.getChild(0));
            return null;
        }
        Environment loopEnvironment = new Environment(environment);
        Lexeme count = new Lexeme(IDENTIFIER, tree.getLineNumber(), "count");
        loopEnvironment.add(count, new Lexeme(NUMBER, tree.getLineNumber(), 0));
        Lexeme countNum = loopEnvironment.lookup(count);
        Lexeme condition = eval(tree.getChild(0), environment);
        while (countNum.getNumVal() < condition.getNumVal()) {
            evalStatementList(tree.getChild(1), loopEnvironment);
            loopEnvironment.update(count, new Lexeme(NUMBER, tree.getLineNumber(), loopEnvironment.lookup(count).getNumVal() + 1));
            countNum = loopEnvironment.lookup(count);
            condition = eval(tree.getChild(0), environment);
        }
        return null;
    }

    private Lexeme evalSimpleBinaryOperator(Lexeme tree, Environment environment) {
        log("evalSimpleBinaryOperator");
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
            case DOUBLE_DIVIDE -> {
                return evalDoubleDivide(tree, environment);
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

    private Lexeme evalBinaryComparator(Lexeme tree, Environment environment) { // TODO REDO
        log("evalBinaryComparator");
        Lexeme left = evalStatement(tree.getChild(0), environment);
        Lexeme right = evalStatement(tree.getChild(1), environment);
        TokenType lType = left.getType();
        TokenType rType = right.getType();

        boolean result;
        switch (tree.getType()) {
            case QUESTION -> result = left.equals(right);
            case NOT_QUESTION -> result = !left.equals(right);
            case APPROX -> {
                if (lType == NUMBER && rType == NUMBER) {
                    result = Math.abs(left.getNumVal() - right.getNumVal()) < (left.getNumVal() + right.getNumVal()) / 2 * 0.05;
                } else {
                    result = false;
                }
            }
            case NOT_APPROX -> {
                if (lType == NUMBER && rType == NUMBER) {
                    result = !(Math.abs(left.getNumVal() - right.getNumVal()) < (left.getNumVal() + right.getNumVal()) / 2 * 0.05);
                } else {
                    result = true;
                }
            }
            case DOUBLE_QUESTION -> result = lType == rType;
            case NOT_DOUBLE_QUESTION -> result = !(lType == rType);
            case GREATER -> {
                switch (lType) {
                    case NUMBER:
                        switch (rType) {
                            case NUMBER -> result = left.getNumVal() > right.getNumVal();
                            case STRING -> result = left.getNumVal() > right.getStringVal().length();
                            case BOOLEAN -> result = left.getNumVal() > (right.getBoolVal() ? 1 : 0);
                            case ARRAY -> result = false;
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    case STRING:
                        switch (rType) {
                            case NUMBER -> result = left.getStringVal().length() > right.getNumVal();
                            case STRING -> result = left.getStringVal().length() > right.getStringVal().length();
                            case BOOLEAN -> result = left.getStringVal().length() > (right.getBoolVal() ? 1 : 0);
                            case ARRAY -> result = false;
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    case BOOLEAN:
                        switch (rType) {
                            case NUMBER -> result = (left.getBoolVal() ? 1 : 0) > right.getNumVal();
                            case STRING -> result = (left.getBoolVal() ? 1 : 0) > right.getStringVal().length();
                            case BOOLEAN -> result = (left.getBoolVal() ? 1 : 0) > (right.getBoolVal() ? 1 : 0);
                            case ARRAY -> result = false;
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    case ARRAY:
                        switch (rType) {
                            case NUMBER, STRING, BOOLEAN -> result = true;
                            case ARRAY -> result = left.arrayVal.size() > right.arrayVal.size();
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    default:
                        Sigma.runtimeError("Could not compute binary operator", tree);
                        return null;
                }
            }
            case LESS -> {
                switch (lType) {
                    case NUMBER:
                        switch (rType) {
                            case NUMBER -> result = left.getNumVal() < right.getNumVal();
                            case STRING -> result = left.getNumVal() < right.getStringVal().length();
                            case BOOLEAN -> result = left.getNumVal() < (right.getBoolVal() ? 1 : 0);
                            case ARRAY -> result = true;
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    case STRING:
                        switch (rType) {
                            case NUMBER -> result = left.getStringVal().length() < right.getNumVal();
                            case STRING -> result = left.getStringVal().length() < right.getStringVal().length();
                            case BOOLEAN -> result = left.getStringVal().length() < (right.getBoolVal() ? 1 : 0);
                            case ARRAY -> result = true;
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    case BOOLEAN:
                        switch (rType) {
                            case NUMBER -> result = (left.getBoolVal() ? 1 : 0) < right.getNumVal();
                            case STRING -> result = (left.getBoolVal() ? 1 : 0) < right.getStringVal().length();
                            case BOOLEAN -> result = (left.getBoolVal() ? 1 : 0) < (right.getBoolVal() ? 1 : 0);
                            case ARRAY -> result = true;
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    case ARRAY:
                        switch (rType) {
                            case NUMBER, STRING, BOOLEAN -> result = false;
                            case ARRAY -> result = left.arrayVal.size() < right.arrayVal.size();
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    default:
                        Sigma.runtimeError("Could not compute binary operator", tree);
                        return null;
                }
            }
            case GEQ, GREATER_QUESTION -> {
                switch (lType) {
                    case NUMBER:
                        switch (rType) {
                            case NUMBER -> result = left.getNumVal() >= right.getNumVal();
                            case STRING -> result = left.getNumVal() >= right.getStringVal().length();
                            case BOOLEAN -> result = left.getNumVal() >= (right.getBoolVal() ? 1 : 0);
                            case ARRAY -> result = false;
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    case STRING:
                        switch (rType) {
                            case NUMBER -> result = left.getStringVal().length() >= right.getNumVal();
                            case STRING -> result = left.getStringVal().length() >= right.getStringVal().length();
                            case BOOLEAN -> result = left.getStringVal().length() >= (right.getBoolVal() ? 1 : 0);
                            case ARRAY -> result = false;
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    case BOOLEAN:
                        switch (rType) {
                            case NUMBER -> result = (left.getBoolVal() ? 1 : 0) >= right.getNumVal();
                            case STRING -> result = (left.getBoolVal() ? 1 : 0) >= right.getStringVal().length();
                            case BOOLEAN -> result = (left.getBoolVal() ? 1 : 0) >= (right.getBoolVal() ? 1 : 0);
                            case ARRAY -> result = false;
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    case ARRAY:
                        switch (rType) {
                            case NUMBER, STRING, BOOLEAN -> result = true;
                            case ARRAY -> result = left.arrayVal.size() >= right.arrayVal.size();
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    default:
                        Sigma.runtimeError("Could not compute binary operator", tree);
                        return null;
                }
            }
            case LEQ, LESS_QUESTION -> {
                switch (lType) {
                    case NUMBER:
                        switch (rType) {
                            case NUMBER -> result = left.getNumVal() <= right.getNumVal();
                            case STRING -> result = left.getNumVal() <= right.getStringVal().length();
                            case BOOLEAN -> result = left.getNumVal() <= (right.getBoolVal() ? 1 : 0);
                            case ARRAY -> result = true;
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    case STRING:
                        switch (rType) {
                            case NUMBER -> result = left.getStringVal().length() <= right.getNumVal();
                            case STRING -> result = left.getStringVal().length() <= right.getStringVal().length();
                            case BOOLEAN -> result = left.getStringVal().length() <= (right.getBoolVal() ? 1 : 0);
                            case ARRAY -> result = true;
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    case BOOLEAN:
                        switch (rType) {
                            case NUMBER -> result = (left.getBoolVal() ? 1 : 0) <= right.getNumVal();
                            case STRING -> result = (left.getBoolVal() ? 1 : 0) <= right.getStringVal().length();
                            case BOOLEAN -> result = (left.getBoolVal() ? 1 : 0) <= (right.getBoolVal() ? 1 : 0);
                            case ARRAY -> result = true;
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    case ARRAY:
                        switch (rType) {
                            case NUMBER, STRING, BOOLEAN -> result = false;
                            case ARRAY -> result = left.arrayVal.size() <= right.arrayVal.size();
                            default -> {
                                Sigma.runtimeError("Could not compute binary operator", tree);
                                return null;
                            }
                        }
                        break;
                    default:
                        Sigma.runtimeError("Could not compute binary operator", tree);
                        return null;
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
        log("evalBooleanBinaryOperator");
        Lexeme left = evalStatement(tree.getChild(0), environment);
        Lexeme right = evalStatement(tree.getChild(1), environment);
        boolean l = isTruthy(left);
        boolean r = isTruthy(right);
        boolean result;
        switch (tree.getType()) {
            case AND_KEYWORD -> result = l && r;
            case OR_KEYWORD -> result = l || r;
            case NAND_KEYWORD -> result = !(l && r);
            case XOR_KEYWORD -> result = l ^ r;
            case XNOR_KEYWORD -> result = l == r;
            default -> {
                error("Problem with binary boolean operator", tree);
                return null;
            }
        }
        return new Lexeme(BOOLEAN, tree.getLineNumber(), result);
    }

    private Lexeme evalIncDec(Lexeme tree, Environment environment) {
        log("evalIncDec");
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
        Lexeme l = evalStatement(tree.getChild(0), environment);
        Lexeme r = evalStatement(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        if (lType == NOTHING || rType == NOTHING) {
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
                        ArrayList<Lexeme> tempR = r.arrayVal;
                        tempR.add(0, l);
                        return new Lexeme(ARRAY, tree.getLineNumber(), tempR);
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
                        ArrayList<Lexeme> tempR = r.arrayVal;
                        tempR.add(0, l);
                        return new Lexeme(ARRAY, tree.getLineNumber(), tempR);
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
                        ArrayList<Lexeme> tempR = r.arrayVal;
                        tempR.add(0, l);
                        return new Lexeme(ARRAY, tree.getLineNumber(), tempR);
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
            Lexeme child = evalStatement(tree.getChild(0), environment);
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
                case NOTHING:
                    return child;
                default:
                    error("Invalid type after unary minus operator", tree);
                    return null;
            }
        } else if (tree.getNumChildren() == 2) {
            Lexeme l = evalStatement(tree.getChild(0), environment);
            Lexeme r = evalStatement(tree.getChild(1), environment);
            TokenType lType = l.getType();
            TokenType rType = r.getType();

            if (lType == NOTHING || rType == NOTHING) {
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
        log("evalNotOperator");
        Lexeme child = evalStatement(tree.getChild(0), environment);
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
            case NOTHING:
                return child;
            default:
                error("Invalid type after not operator", tree);
                return null;
        }
    }

    private Lexeme evalTimes(Lexeme tree, Environment environment) {
        log("evalTimes");
        Lexeme l = evalStatement(tree.getChild(0), environment);
        Lexeme r = evalStatement(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        if (lType == NOTHING || rType == NOTHING) {
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
                        return new Lexeme(ARRAY, tree.getLineNumber(), l.getBoolVal() ? r.arrayVal : new ArrayList<>());
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
                        return new Lexeme(ARRAY, tree.getLineNumber(), l.getBoolVal() ? r.arrayVal : new ArrayList<>());
                    case ARRAY:
                        tempL = l.arrayVal;
                        tempL.addAll(r.arrayVal);
                        ArrayList<Lexeme> newList = new ArrayList<>();
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
        Lexeme l = evalStatement(tree.getChild(0), environment);
        Lexeme r = evalStatement(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        if (lType == NOTHING || rType == NOTHING) {
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
                ArrayList<Lexeme> temp;
                switch (rType) {
                    case NUMBER:
                        temp = new ArrayList<>(l.arrayVal.subList(0, (int) (l.arrayVal.size() / r.getNumVal())));
                        return new Lexeme(ARRAY, tree.getLineNumber(), temp);
                    case STRING:
                        temp = new ArrayList<>(l.arrayVal.subList(0, l.arrayVal.size() / r.getStringVal().length()));
                        return new Lexeme(ARRAY, tree.getLineNumber(), temp);
                    case BOOLEAN:
                        return new Lexeme(ARRAY, tree.getLineNumber(), r.getBoolVal() ? l.arrayVal : new ArrayList<>());
                    case ARRAY:
                        temp = new ArrayList<>(l.arrayVal.subList(0, l.arrayVal.size() / r.arrayVal.size()));
                        return new Lexeme(ARRAY, tree.getLineNumber(), temp);
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
        Lexeme l = evalStatement(tree.getChild(0), environment);
        Lexeme r = evalStatement(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        if (lType == NOTHING || rType == NOTHING) {
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
        Lexeme l = evalStatement(tree.getChild(0), environment);
        Lexeme r = evalStatement(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        if (lType == NOTHING || rType == NOTHING) {
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
        Lexeme l = evalStatement(tree.getChild(0), environment);
        Lexeme r = evalStatement(tree.getChild(1), environment);
        TokenType lType = l.getType();
        TokenType rType = r.getType();

        if (lType == NOTHING || rType == NOTHING) {
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
                        return new Lexeme(STRING, tree.getLineNumber(), l.getStringVal().substring(0, l.getStringVal().length() % r.getStringVal().length()));
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

    private Lexeme evalArray(Lexeme tree, Environment environment) {
        ArrayList<Lexeme> result = new ArrayList<>();
        for (Lexeme element : tree.arrayVal) {
            result.add(evalStatement(element, environment));
        }
        return new Lexeme(ARRAY, tree.getLineNumber(), result);
    }

    private boolean isTruthy(Lexeme lexeme) {
        if (lexeme.getType() == NOTHING) return false;
        return ((lexeme.getNumVal() == null ? 1 : lexeme.getNumVal()) != 0) // if non null, not 0
                && !((lexeme.getStringVal() == null ? " " : lexeme.getStringVal()).equals("")) // if non-null, not empty
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