public class HashTableRecord {
    public Token data;
    public String type;
    public boolean isInit;
    public boolean isUsed;

    public HashTableRecord(Token data, String type) {
        this.data = data;
        this.type = type;
        isInit = false;
        isUsed = false;
    }
}
