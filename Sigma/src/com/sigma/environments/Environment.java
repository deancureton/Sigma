package com.sigma.environments;

import com.sigma.Sigma;
import com.sigma.lexicalAnalysis.*;

import static com.sigma.lexicalAnalysis.TokenType.*;

import java.util.ArrayList;

public class Environment {
    Environment parent;
    public ArrayList<Lexeme> names = new ArrayList<>();
    public ArrayList<Lexeme> values = new ArrayList<>();

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public Environment() {
        this(null);
    }

    public void add(Lexeme name, Lexeme value) {
        if (this.softLookup(name)) {
            Sigma.referenceError("Variable " + name.getStringVal() + " already exists", name);
        }
        names.add(name);
        values.add(value);
    }

    public void update(Lexeme name, Lexeme value) {
        int index = -1;
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(name)) index = i;
        }
        if (index == -1) {
            if (this.parent == null) {
                Sigma.referenceError("Variable " + name.getStringVal() + " not found", name);
            } else {
                this.parent.update(name, value);
            }
        } else {
            int lineNumber = values.get(index).getLineNumber();
            values.remove(index);
            if (value.getNumVal() != null) {
                values.add(index, new Lexeme(NUMBER, lineNumber, value.getNumVal()));
                return;
            }
            if (value.getStringVal() != null) {
                values.add(index, new Lexeme(STRING, lineNumber, value.getStringVal()));
                return;
            }
            if (value.getBoolVal() != null) {
                values.add(index, new Lexeme(BOOLEAN, lineNumber, value.getBoolVal()));
            }
        }
    }

    public Lexeme lookup(Lexeme name) {
        int index = -1;
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(name)) index = i;
        }
        if (index == -1) {
            if (this.parent == null) {
                Sigma.referenceError("Variable " + name.getStringVal() + " not found", name);
                return null;
            } else {
                return this.parent.lookup(name);
            }
        } else {
            return values.get(index);
        }
    }

    private boolean softLookup(Lexeme name) {
        int index = -1;
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(name)) index = i;
        }
        if (index == -1) {
            if (this.parent == null) {
                return false;
            } else {
                return this.parent.softLookup(name);
            }
        } else {
            return true;
        }
    }

    public void extend(Lexeme param, Lexeme arg) {
        if (arg.getNumChildren() != param.getNumChildren()) {
            Sigma.runtimeError("Invalid number of function arguments", arg);
            return;
        }
        for (int i = 0; i < param.getNumChildren(); i++) {
            this.add(param.getChild(i), arg.getChild(i));
        }
    }

    public String toString() {
        if (this.parent == null)
            return "ID: " + this.hashCode() + "\nNames: " + this.names.toString() + "\nValues: " + this.values.toString();
        else
            return "ID: " + this.hashCode() + "\nParent: " + this.parent.hashCode() + "\nNames: " + this.names.toString() + "\nValues: " + this.values.toString();
    }
}