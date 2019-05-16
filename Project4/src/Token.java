/**
 * Represents a syntactic token for use in
 * a compiler.
 */
public class Token {
    public String type; // BOOL, ID, CHAR, etc.
    public String data; // the actual value the token represents
    public int lineNum;
    public int position;
    public int scope;

    public Token (String type, String data, int lineNum, int position) {
        this.type = type;
        this.data = data;
        this.lineNum = lineNum;
        this.position = position;
    }
}
