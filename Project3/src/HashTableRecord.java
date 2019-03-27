public class HashTableRecord {
    public String data;
    public boolean isInit;
    public boolean isUsed;

    public HashTableRecord(String data) {
        this.data = data;
        isInit = false;
        isUsed = false;
    }
}
