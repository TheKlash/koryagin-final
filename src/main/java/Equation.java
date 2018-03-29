import sun.rmi.runtime.Log;

import java.util.ArrayList;

public class Equation {
    private String leftVariable;
    private String rootVariable;
    private Token[] equationTokens;
    private String[] roots;
    private ArrayList<Slog> slogs;

    private String error;
    private boolean errorFlag = false;
    private int errorPosition;

    private double a;
    private double b;
    private double c;

    public Equation() {
        error = "";
    }

    public Equation(Token[] equationTokens) {
        this.equationTokens = equationTokens;
    }

    public String getLeftVariable() {
        return leftVariable;
    }

    public void setLeftVariable(String leftVariable) {
        this.leftVariable = leftVariable;
    }

    public String getRootVariable() {
        return rootVariable;
    }

    public void setRootVariable(String rootVariable) {
        this.rootVariable = rootVariable;
    }

    public Token[] getEquationTokens() {
        return equationTokens;
    }

    public void setEquationTokens(Token[] equationTokens) {
        this.equationTokens = equationTokens;
    }

    public String[] getRoots() {
        return roots;
    }

    public void setRoots(String[] roots) {
        this.roots = roots;
    }

    public String getError() {
        return error;
    }

    public boolean isErrorFlag() {
        return errorFlag;
    }

    public int getErrorPosition() {
        return errorPosition;
    }

    public void findRoots(){

        leftVariable = equationTokens[0].getValue();
        String slogString = "";
        Slog slog = new Slog();
        boolean bracketsOpen = false;

        for (int i = 2; i < equationTokens.length; i++) {
            Token token = equationTokens[i];
            Token pastToken = equationTokens[i-1];
            if (token.getType() == TokenType.VAR) {
                if (rootVariable == null)
                    rootVariable = token.getValue();
                if (slog.getType() == SlogType.A) {
                    error = "Степень переменной больше двух!";
                    errorPosition = token.getCharNum();
                    errorFlag = true;
                    break;
                }
                else if (slog.getType() == SlogType.B) {
                    slog.setType(SlogType.A);
                }
                else
                    slog.setType(SlogType.B);
                slogString += token.getValue();
            }
            else if (token.getType() == TokenType.DIVIDE) {
                slogString += "/";
            }
            else if (token.getType() == TokenType.MULT) {
                slogString += "*";
            }
            else if (token.getType() == TokenType.POW) {
                slogString += "^";
            }
            else if (token.getType() == TokenType.NUM) {
                String numString = token.getValue();
                if (pastToken.getType() == TokenType.POW) {
                    int pow = Integer.parseInt(numString);
                    if (pow == 2) {
                        if (slog.getType() !=  SlogType.A)
                            slog.setType(SlogType.A);
                        else {
                            error = "Степень переменной больше двух!";
                            errorPosition = token.getCharNum();
                            errorFlag = true;
                            break;
                        }
                    }
                    else if (pow == 1) {
                        if (slog.getType() == SlogType.A) {
                            error = "Степень переменной больше двух!";
                            errorPosition = token.getCharNum();
                            errorFlag = true;
                            break;
                        }
                        else if (slog.getType() == SlogType.B) {
                            slog.setType(SlogType.A);
                        }
                        else
                            slog.setType(SlogType.B);
                    }
                }
                slogString += numString;
            }
            else if (token.getType() == TokenType.SIN) {
                slogString += "sin";
            }
            else if (token.getType() == TokenType.COS) {
                slogString += "cos";
            }
            else if (token.getType() == TokenType.ABS) {
                slogString += "abs";
            }
            else if (token.getType() == TokenType.RIGHT_BRACKET) {
                slogString += ")";
                bracketsOpen = false;
            }
            else if (token.getType() == TokenType.LEFT_BRACKET) {
                slogString += "(";
                bracketsOpen = true;
            }
            else if (token.getType() == TokenType.PLUS) {
                if (!slogString.equals("")){
                    try {
                        countSlogValue(slog, slogString);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("FAILED SLOG: " + slogString);
                    }
                }
                slog = new Slog();
                slogString = "";
            }
            else if (token.getType() == TokenType.MINUS) {
                if (bracketsOpen) {
                    slogString += "-";
                }
                else {
                    if (!slogString.equals("")) {
                        try {
                            countSlogValue(slog, slogString);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("FAILED SLOG: " + slogString);
                        }
                    }
                    slog = new Slog();
                    slogString = "";
                    slog.setPositivity(Positivity.NEGATIVE);
                }
            }
            if (equationTokens.length - i == 1) {
                try {
                    countSlogValue(slog, slogString);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("FAILED SLOG: " + slogString);
                }
                slog = new Slog();
                slogString = "";
            }
        }

        if ((c == 0 && a == 0) || (c == 0 && b == 0) || (a==0 && b == 0)) {
            roots = new String[1];
            roots[0] = Double.toString(0.0);
        } else if (b != 0 && c != 0 && a == 0)  {
            roots = new String[1];
            roots[0] = Double.toString(-b / c);
        } else {
            double d = b * b - 4 * a * c;
            if (d == 0) {
                roots = new String[1];
                roots[0] = Double.toString(-b / 2*a);
            } else if (d > 0) {
                double x1 = (-b - Math.sqrt(d)) / (2 * a);
                double x2 = (-b + Math.sqrt(d)) / (2 * a);

                roots = new String[2];
                roots[0] = Double.toString(x1);
                roots[1] = Double.toString(x2);
            }
        }
    }

    private void countSlogValue(Slog slog, String raw) throws Exception {
        System.out.println("Current Slog: " + raw);
        slog.setRawString(raw);
        double counted = slog.getValue();
        if (slog.getPositivity() == Positivity.NEGATIVE)
            counted *= -1;
        if (slog.getType() == SlogType.A)
            a += counted;
        else if (slog.getType() == SlogType.B)
            b += counted;
        else
            c += counted;
    }


    private class Slog {
        double value;
        private SlogType type;
        private String rawString;
        private Positivity positivity;

        public Slog() {
            type = SlogType.C;
            rawString = "";
            positivity = Positivity.POSITIVE;
        }

        public String getRawString() {
            return rawString;
        }

        public void setRawString(String rawString) {
            this.rawString = rawString;
        }

        public double getValue() throws Exception {
            MatchParser parser = new MatchParser();
            parser.setVariable(rootVariable);
            value = parser.Parse(rawString);
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public SlogType getType() {
            return type;
        }

        public void setType(SlogType type) {
            this.type = type;
        }

        public Positivity getPositivity() {
            return positivity;
        }

        public void setPositivity(Positivity positivity) {
            this.positivity = positivity;
        }
    }

    private enum SlogType{
        A,
        B,
        C
    }

    private enum Positivity {
        POSITIVE,
        NEGATIVE,
    }

}
