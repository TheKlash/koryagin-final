public class Token {
    private TokenType type;
    private String value;
    private int charNum;

    public Token() {
    }

    public Token(TokenType type, String value, int charNum) {
        this.type = type;
        this.value = value;
        this.charNum = charNum;
    }

    public Token(TokenType type, int charNum) {
        this.type = type;
        this.charNum = charNum;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getCharNum() {
        return charNum;
    }

    public void setCharNum(int charNum) {
        this.charNum = charNum;
    }
}
