public class StaticVarEntry {
    String id;
    String tempAddress;
    String permAddress;
    int scope;

    public StaticVarEntry(String id, int staticIndex, int scope) {
        this.id = id;
        this.tempAddress = "T"+staticIndex;
        this.scope = scope;
    }

    public void setPermAddress(String permAddress) {
        this.permAddress = permAddress;
    }
}
