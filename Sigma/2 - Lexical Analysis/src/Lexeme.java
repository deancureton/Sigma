public class Lexeme {
    private TokenType type;
    private int lineNumber;

    private String stringVal;
    private double numVal; // Sigma only supports a single real number type
    private boolean boolVal;

    public Lexeme(String lexemeValue, TokenType lexemeType, int lexemeLineNumber) {
        stringVal = lexemeValue;
        type = lexemeType;
        lineNumber = lexemeLineNumber;
    }

    public Lexeme(int lexemeValue, TokenType lexemeType, int lexemeLineNumber) {
        numVal = lexemeValue;
        type = lexemeType;
        lineNumber = lexemeLineNumber;
    }

    public Lexeme(double lexemeValue, TokenType lexemeType, int lexemeLineNumber) {
        numVal = lexemeValue;
        type = lexemeType;
        lineNumber = lexemeLineNumber;
    }

    public Lexeme(boolean lexemeValue, TokenType lexemeType, int lexemeLineNumber) {
        boolVal = lexemeValue;
        type = lexemeType;
        lineNumber = lexemeLineNumber;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getStringVal() {
        return stringVal;
    }

    public void setStringVal(String stringVal) {
        this.stringVal = stringVal;
    }

    public double getNumVal() {
        return numVal;
    }

    public void setNumVal(double numVal) {
        this.numVal = numVal;
    }

    public boolean getBoolVal() {
        return boolVal;
    }

    public void setBoolVal(boolean boolVal) {
        this.boolVal = boolVal;
    }

    public String toString() {
        String outputValue = "";
        String output = "";
        switch (this.getType().name()) {
            case "STRING":
                outputValue = getStringVal();
                break;
            case "NUMBER":
                outputValue = String.valueOf(getNumVal());
                break;
            case "BOOLEAN":
                outputValue = String.valueOf(getBoolVal());
                break;
        }
        if (outputValue.equals("")) {
            output = "Type: " + this.getType().name() + "\nLine Number: " + lineNumber;
        } else {
            output = "Type: " + this.getType().name() + "\nValue: " + outputValue + "\nLine Number: " + lineNumber;
        }
        System.out.println(output);
        return output;
    }
}