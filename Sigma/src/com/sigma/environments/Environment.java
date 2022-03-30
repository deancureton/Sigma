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

    public void add(Lexeme name, Lexeme value) {
        names.add(name);
        values.add(value);
    }

    public void modify(Lexeme name, Lexeme value, Lexeme target) {
        int index = -1;
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(name)) index = i;
        }
        if (index == -1) {
            if (this.parent == null) {
                Sigma.referenceError("Variable " + name.getStringVal() + " not found", target);
            } else {
                this.parent.modify(name, value, target);
            }
        } else {
            values.get(index).setBoolVal(value.getBoolVal());
            values.get(index).setNumVal(value.getNumVal());
            values.get(index).setStringVal(value.getStringVal());
        }
    }

    public Lexeme retrieve(Lexeme name, Lexeme target) {
        int index = -1;
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(name)) index = i;
        }
        if (index == -1) {
            if (this.parent == null) {
                Sigma.referenceError("Variable " + name.getStringVal() + " not found", target);
                return null;
            } else {
                return this.parent.retrieve(name, target);
            }
        } else {
            return values.get(index);
        }
    }

    public String toString() {
        if (this.parent == null) return "ID: " + this.hashCode() + "\nNames: " + this.names.toString() + "\nValues: " + this.values.toString();
        else return "ID: " + this.hashCode() + "\nParent: " + this.parent.hashCode() + "\nNames: " + this.names.toString() + "\nValues: " + this.values.toString();
    }
}