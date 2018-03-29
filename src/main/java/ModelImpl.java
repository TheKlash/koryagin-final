import java.util.ArrayList;

public class ModelImpl implements Model, View.Callback {

    //Когда будет выбрасываться исключение, необходимо, чтобы переменные стирались
    private ArrayList<Equation> equations;
    private ArrayList<String> rawEquations;
    private ArrayList<String> multiplicies;
    private Callback callback;

    public ModelImpl(Callback callback) {
        this.callback = callback;
        equations = new ArrayList<Equation>();
        rawEquations = new ArrayList<String>();
        multiplicies = new ArrayList<String>();
    }

    private void clearAll() {
        equations = new ArrayList<Equation>();
        rawEquations = new ArrayList<String>();
        multiplicies = new ArrayList<String>();
    }

    public void solve(String lines) throws RuntimeException {
        clearAll();
        ArrayList<Token> tokens = tokenize(lines);
        if (tokens.get(tokens.size() - 1).getType().equals(TokenType.LEX_ERROR)) {
            clearAll();
            callback.onFailure("Неизвестный символ", tokens.get(tokens.size() - 1));
        } else {
            tokens = syntaxAnaliz(tokens);
            Token lastToken = tokens.get(tokens.size() - 1);
            if (lastToken.getType().equals(TokenType.SYN_ERROR)) {
                clearAll();
                callback.onFailure(lastToken.getValue(), lastToken);
            }
            else {
                for (Equation equation: equations)
                    equation.findRoots();
                callback.onSuccess(equations);
                clearAll();
            }
        }
    }

    private ArrayList<Token> tokenize(String lines) {
        ArrayList<Token> tokens = new ArrayList<Token>();
        char[] chars = lines.toLowerCase().toCharArray();
        int i = 0;
        while (chars[i] == ' ')
            i++;
        while (i < chars.length) {
            if (chars[i] == 'b' && chars[i + 1] == 'e' && chars[i + 2] == 'g' && chars[i + 3] == 'i' && chars[i + 4] == 'n') {
                tokens.add(new Token(TokenType.BEGIN, i));
                i += 5;
            } else if (chars[i] == 'e' && chars[i + 1] == 'n' && chars[i + 2] == 'd') {
                tokens.add(new Token(TokenType.END, i));
                i += 3;
            } else if (chars[i] == 'а' && chars[i + 1] == 'н' && chars[i + 2] == 'а' && chars[i + 3] == 'л' && chars[i + 4] == 'и' && chars[i + 5] == 'з') {
                tokens.add(new Token(TokenType.ANALIZ, i));
                i += 6;
            } else if (chars[i] == 'с' && chars[i + 1] == 'и' && chars[i + 2] == 'н' && chars[i + 3] == 'т' && chars[i + 4] == 'е' && chars[i + 5] == 'з') {
                tokens.add(new Token(TokenType.SINTEZ, i));
                i += 6;
            } else if (chars[i] == 's' && chars[i + 1] == 'i' && chars[i + 2] == 'n') {
                tokens.add(new Token(TokenType.SIN, i));
                i += 3;
            } else if (chars[i] == 'c' && chars[i + 1] == 'o' && chars[i + 2] == 's') {
                tokens.add(new Token(TokenType.COS, i));
                i += 3;
            } else if (chars[i] == 'a' && chars[i + 1] == 'b' && chars[i + 2] == 's') {
                tokens.add(new Token(TokenType.ABS, i));
                i += 3;
            } else if (chars[i] == '+') {
                tokens.add(new Token(TokenType.PLUS, i));
                i++;
            } else if (chars[i] == '-') {
                tokens.add(new Token(TokenType.MINUS, i));
                i++;
            } else if (chars[i] == '*') {
                tokens.add(new Token(TokenType.MULT, i));
                i++;
            } else if (chars[i] == '/') {
                tokens.add(new Token(TokenType.DIVIDE, i));
                i++;
            } else if (chars[i] == '^') {
                tokens.add(new Token(TokenType.POW, i));
                i++;
            } else if (chars[i] == '=') {
                tokens.add(new Token(TokenType.EQULAS, i));
                i++;
            } else if (chars[i] == '\n') {
                tokens.add(new Token(TokenType.EOL, i));
                i++;
            } else if (chars[i] == ',') {
                tokens.add(new Token(TokenType.COMA, i));
                i++;
            } else if (chars[i] == '(') {
                tokens.add(new Token(TokenType.LEFT_BRACKET, i));
                i++;
            } else if (chars[i] == ')') {
                tokens.add(new Token(TokenType.RIGHT_BRACKET, i));
                i++;
            } else if (chars[i] == ' ') {
                tokens.add(new Token(TokenType.SPACE, i));
                i++;
            } else if ((chars[i] >= 'a' && chars[i] <= 'z') || (chars[i] >= 'а' && chars[i] <= 'я')) {
                int varFirst = i;
                String value = "";
                value += chars[i];
                i++;
                while ((chars[i] >= 'a' && chars[i] <= 'z') || (chars[i] >= 'а' && chars[i] <= 'я')
                        || (chars[i] >= '0' && chars[i] <= '9')) {
                    value += chars[i];
                    i++;
                }
                tokens.add(new Token(TokenType.VAR, value, varFirst));
            } else if (chars[i] >= '0' && chars[i] <= '9') {
                String value = "";
                value += chars[i];
                int numFirst = i;
                i++;
                while (chars[i] >= '0' && chars[i] <= '9') {
                    value += chars[i];
                    i++;
                }
                tokens.add(new Token(TokenType.NUM, value, numFirst));
            } else {
                tokens.add(new Token(TokenType.LEX_ERROR, String.valueOf(chars[i]), i));
                break;
            }
        }
        return tokens;
    }

    private ArrayList<Token> syntaxAnaliz(ArrayList<Token> tokensList) {
        ArrayList<Token> returnTokens = new ArrayList<Token>();
        Token[] tokens = tokensList.toArray(new Token[0]);
        int i = 0;
        if (tokens[0].getType() != TokenType.BEGIN) {
            returnTokens.add(new Token(TokenType.SYN_ERROR, "Программа должна начинаться с Begin!", tokens[0].getCharNum()));
        } else {
            //регион уравнений

            ArrayList<String> leftVariables = new ArrayList<String>();
            ArrayList<String> rightVariables = new ArrayList<String>();
            i++;
            while (tokens[i].getType() != TokenType.SINTEZ && tokens[i].getType() != TokenType.ANALIZ) {
                while (tokens[i].getType() == TokenType.EOL || tokens[i].getType() == TokenType.SPACE) {
                    i++;
                }
                ArrayList<Token> equationTokens = new ArrayList<Token>();
                while (tokens[i].getType() != TokenType.EOL) {
                    if (tokens[i].getType() != TokenType.SPACE) {
                        returnTokens.add(tokens[i]);
                        equationTokens.add(tokens[i]);
                    }
                    i++;
                }

                Equation equation = new Equation();
                Token[] equationTokensArray = equationTokens.toArray(new Token[equationTokens.size()]);
                if (equationTokensArray.length != 0) {
                    if (equationTokensArray[0].getType() != TokenType.VAR) {
                        returnTokens.add(
                                new Token(TokenType.SYN_ERROR,
                                        "Ожидалось начало уравнения!", equationTokensArray[0].getCharNum()));
                        return returnTokens;
                    }
                    else {
                        leftVariables.add(equationTokensArray[0].getValue());
                    }
                    if (equationTokensArray[1].getType() != TokenType.EQULAS) {
                        returnTokens.add(
                                new Token(TokenType.SYN_ERROR,
                                        "В уравнении после левой переменной должно идти равно!", equationTokensArray[1].getCharNum()));
                        return returnTokens;
                    }

                    if (equationTokensArray.length == 2 ||
                            (equationTokensArray[2].getType() != TokenType.MINUS &&
                                    equationTokensArray[2].getType() != TokenType.VAR &&
                                    equationTokensArray[2].getType() != TokenType.NUM &&
                                    equationTokensArray[2].getType() != TokenType.SIN &&
                                    equationTokensArray[2].getType() != TokenType.COS &&
                                    equationTokensArray[2].getType() != TokenType.ABS)
                            ) {
                        returnTokens.add(
                                new Token(TokenType.SYN_ERROR,
                                        "В уравнении после равно должна идти перменная, число, минус или функция!", equationTokensArray[2].getCharNum()));
                        return returnTokens;
                    }
                }

                boolean bracketsOpen = false;
                for (int j = 3; j < equationTokensArray.length; j++) {
                    TokenType type = equationTokensArray[j].getType();

                    if (j + 1 != equationTokensArray.length) {
                        TokenType nextType = equationTokensArray[j + 1].getType();
                        if (type == TokenType.PLUS ||
                                type == TokenType.MINUS ||
                                type == TokenType.MULT ||
                                type == TokenType.DIVIDE) {
                            if (nextType != TokenType.VAR &&
                                    nextType != TokenType.NUM &&
                                    nextType != TokenType.SIN &&
                                    nextType != TokenType.COS &&
                                    nextType != TokenType.ABS) {
                                returnTokens.add(
                                        new Token(TokenType.SYN_ERROR,
                                                "В уравнении после оператора должно идти число, переменная или фунция!", equationTokensArray[j + 1].getCharNum()));
                                return returnTokens;
                            }
                            else if (bracketsOpen && type != TokenType.MINUS && equationTokensArray[j-1].getType() != TokenType.LEFT_BRACKET) {
                                returnTokens.add(
                                        new Token(TokenType.SYN_ERROR,
                                                "Внутри функций запрещены операторы!", equationTokensArray[j].getCharNum())
                                );
                                return returnTokens;
                            }
                        } else if (type == TokenType.NUM) {
                            if (nextType != TokenType.PLUS &&
                                    nextType != TokenType.MINUS &&
                                    nextType != TokenType.MULT &&
                                    nextType != TokenType.DIVIDE &&
                                    nextType != TokenType.POW &&
                                    nextType != TokenType.RIGHT_BRACKET) {
                                        returnTokens.add(new Token(TokenType.SYN_ERROR, "В уравнении после числа должен идти оператор!", equationTokensArray[j + 1].getCharNum()));
                                        return returnTokens;
                            }
                        } else if (type == TokenType.VAR) {
                            if (nextType != TokenType.PLUS &&
                                    nextType != TokenType.MINUS &&
                                    nextType != TokenType.MULT &&
                                    nextType != TokenType.DIVIDE &&
                                    nextType != TokenType.POW ) {
                                        returnTokens.add(new Token(TokenType.SYN_ERROR, "В уравнении после переменной (не стоящей в конце строки) должен идти оператор!", equationTokensArray[j + 1].getCharNum()));
                                        return returnTokens;
                            }
                            else if (bracketsOpen) {
                                returnTokens.add(
                                        new Token(TokenType.SYN_ERROR,
                                                "Внутри функций запрещены переменные!", equationTokensArray[j].getCharNum())
                                );
                                return returnTokens;
                            }
                            else {
                                String rootVariable = equation.getRootVariable();
                                String tokenValue = equationTokensArray[j].getValue();
                                if (rootVariable == null) {
                                    equation.setRootVariable(tokenValue);
                                    rightVariables.add(equationTokensArray[j].getValue());
                                }
                                else if (!rootVariable.equals(tokenValue)) {
                                    returnTokens.add(new Token(TokenType.SYN_ERROR, "В правой части уравнения уже введена переменная " + rootVariable + ", введение переменной " + tokenValue + " невозможно!", equationTokensArray[j].getCharNum()));
                                    return returnTokens;
                                }
                            }
                        }

                        else if (type == TokenType.RIGHT_BRACKET) {
                            if (!bracketsOpen) {
                                returnTokens.add(
                                        new Token(TokenType.SYN_ERROR,
                                                "Найдена правая скобка, не найдена левая!", equationTokensArray[j].getCharNum())
                                );
                                return returnTokens;
                            }
                            else
                                bracketsOpen = false;
                        }
                        else if (type == TokenType.POW) {
                            if (nextType != TokenType.NUM) {
                                if (bracketsOpen) {
                                    returnTokens.add(
                                            new Token(TokenType.SYN_ERROR,
                                                    "Внутри функций запрешено возведение в степень!", equationTokensArray[j].getCharNum())
                                    );
                                    return returnTokens;
                                }
                                returnTokens.add(new Token(TokenType.SYN_ERROR, "В уравнении после возведения в степень должно идти число!", equationTokensArray[j + 1].getCharNum()));
                                return returnTokens;
                            }
                        } else if (type == TokenType.SIN || type == TokenType.COS || type == TokenType.ABS) {
                            if (nextType != TokenType.LEFT_BRACKET) {
                                returnTokens.add(new Token(TokenType.SYN_ERROR, "В уравнении после функции должны открываться скобки!", equationTokensArray[j + 1].getCharNum()));
                                return returnTokens;
                            }
                            else {
                                bracketsOpen = true;
                            }
                        }
                    } else {
                        if (bracketsOpen) {
                            returnTokens.add(new Token(TokenType.SYN_ERROR, "Не закрыта скобка!", equationTokensArray[j].getCharNum()));
                            return returnTokens;
                        } else  if (!(type == TokenType.NUM || type == TokenType.VAR)) {
                            returnTokens.add(new Token(TokenType.SYN_ERROR, "Уравнение должно заканчиваться числом или переменной!", equationTokensArray[j].getCharNum()));
                            return returnTokens;
                        } else if (type == TokenType.VAR) {
                            String rootVariable = equation.getRootVariable();
                            String tokenValue = equationTokensArray[j].getValue();
                            if (rootVariable == null) {
                                equation.setRootVariable(tokenValue);
                                rightVariables.add(equationTokensArray[j].getValue());
                            }
                            else if (!rootVariable.equals(tokenValue)) {
                                returnTokens.add(new Token(TokenType.SYN_ERROR, "В правой части уравнения уже введена переменная " + rootVariable + ", введение переменной " + tokenValue + " невозможно!", equationTokensArray[j].getCharNum()));
                                return returnTokens;
                            }

                        }
                    }
                    equation.setEquationTokens(equationTokensArray);
                }
                equations.add(equation);
                //equation = new Equation();
                i++;
            }


            //Регион множеств
            Token[] remainingTokens = new Token[tokens.length - i];
            ArrayList<String> declaredVariables = new ArrayList<String>();
            int k = 0;
            for (int j = 0; j < tokens.length - i; j++) {
                if (tokens[i+j].getType() != TokenType.SPACE && tokens[i+j].getType() != TokenType.EOL) {
                    remainingTokens[k] = tokens[i + j];
                    k++;
                }
            }

            int counter = 0;
            for (int m = 0; m <= k-1; m++) {
                TokenType type = remainingTokens[m].getType();
                if (type == TokenType.ANALIZ || type == TokenType.SINTEZ){
                    if (m == k)
                    {
                        returnTokens.add(new Token(TokenType.SYN_ERROR, "Ожидалось End, а не Анализ ил Синтез!", remainingTokens[m].getCharNum()));
                        return returnTokens;
                    }
                    if (counter != 0) {
                        returnTokens.add(new Token(TokenType.SYN_ERROR, "Синтез и Анализ должны быть в начале множества!!", remainingTokens[m].getCharNum()));
                        return returnTokens;
                    }
                    else
                        counter++;
                }
                 else if (type == TokenType.VAR) {
                    if (m == k-1)
                    {
                        returnTokens.add(new Token(TokenType.SYN_ERROR, "Ожидалось End, а не переменная!", remainingTokens[m].getCharNum()));
                        return returnTokens;
                    }
                    if (counter != 1) {
                        returnTokens.add(new Token(TokenType.SYN_ERROR, "Найдена переменная, ожидалось - Анализ или Синтез!", remainingTokens[m].getCharNum()));
                        return returnTokens;
                    }
                    else {
                        if (remainingTokens[m-1].getType() == TokenType.ANALIZ) {
                            String varValue = remainingTokens[m].getValue();
                            if (!rightVariables.contains(varValue)){
                                returnTokens.add(new Token(TokenType.SYN_ERROR, "Переменные правых частей уравнений должны объявляться после слова Анализ!", remainingTokens[m].getCharNum()));
                                return returnTokens;
                            }
                            else {
                                rightVariables.remove(varValue);
                            }
                        }
                        else if (remainingTokens[m-1].getType() == TokenType.SINTEZ) {
                            String varValue = remainingTokens[m].getValue();
                            if (!leftVariables.contains(varValue)){
                                returnTokens.add(new Token(TokenType.SYN_ERROR, "Переменные левых частей уравнений должны объявляться после слова Синтез!", remainingTokens[m].getCharNum()));
                                return returnTokens;
                            }
                            else {
                                leftVariables.remove(varValue);
                            }
                        }
                        else if (declaredVariables.contains(remainingTokens[m].getValue())) {
                            returnTokens.add(new Token(TokenType.SYN_ERROR, "Переменная уже объявлена выше!", remainingTokens[m].getCharNum()));
                            return returnTokens;
                        }
                        counter++;
                    }
                }
                else if (type == TokenType.COMA) {
                    if (m == k-1)
                    {
                        returnTokens.add(new Token(TokenType.SYN_ERROR, "Ожидалось End, а не запятая!", remainingTokens[m].getCharNum()));
                        return returnTokens;
                    }
                    else if (counter != 2) {
                        returnTokens.add(new Token(TokenType.SYN_ERROR, "Запятая должна идти после переменной!", remainingTokens[m].getCharNum()));
                        return returnTokens;
                    }
                    else
                        counter = 0;
                }
                else if (type == TokenType.SPACE || type == TokenType.EOL) {
                    /*
                    if (m == k)
                    {
                        returnTokens.add(new Token(TokenType.SYN_ERROR, "Ожидалось End, пробел или конец строки!", remainingTokens[m].getCharNum()));
                        return returnTokens;
                    }
                    */
                }
                else if (type == TokenType.END) {
                    if (m!=k-1){
                        returnTokens.add(new Token(TokenType.SYN_ERROR, "End ожидалось в конце программы!", remainingTokens[m].getCharNum()));
                        return returnTokens;
                    }
                    if (leftVariables.size() != 0 || rightVariables.size() != 0) {
                        String first = "Не все перменные объявлены!\n";
                        String second = "Необъявленные переменные: ";
                        if (leftVariables.size() != 0)
                            for (String var: leftVariables)
                                second = second + var + " ";
                        if (rightVariables.size() !=0)
                            for (String var: rightVariables)
                                second = second + var + " ";
                        returnTokens.add(new Token(TokenType.SYN_ERROR,  first + second, remainingTokens[m].getCharNum()));
                        return returnTokens;
                    }
                }
                else {
                    returnTokens.add(new Token(TokenType.SYN_ERROR, "Данный операнд запрещен во множествах!", remainingTokens[m].getCharNum()));
                    return returnTokens;
                }
            }


        }
                return  returnTokens;
    }

}

