public class HashTableRecord {
    public Token data;
    public String type;
    public boolean isInit;
    public boolean isUsed;
    public int scope;

    public HashTableRecord(Token data, String type, int scope) {
        this.data = data;
        this.type = type;
        this.scope = scope;
        isInit = false;
        isUsed = false;
    }
}
