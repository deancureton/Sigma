package com.sigma.environments;

import com.sigma.Sigma;
import com.sigma.lexicalAnalysis.*;

import java.util.ArrayList;
import java.util.Objects;

public class Environment {
    Environment parent;
    ArrayList<Lexeme> names = new ArrayList<>();
    ArrayList<Lexeme> values = new ArrayList<>();

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
            values.get(index).setBoolVal(value.getBoolVal());
            values.get(index).setNumVal(value.getNumVal());
            values.get(index).setStringVal(value.getStringVal());
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

    public String toString() {
        if (this.parent == null)
            return "ID: " + this.hashCode() + "\nNames: " + this.names.toString() + "\nValues: " + this.values.toString();
        else
            return "ID: " + this.hashCode() + "\nParent: " + this.parent.hashCode() + "\nNames: " + this.names.toString() + "\nValues: " + this.values.toString();
    }
}