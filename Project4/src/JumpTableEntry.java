public class JumpTableEntry {
    String id;
    int shift;

    public JumpTableEntry(int jumpsCount, int shift) {
        this.id = "J"+jumpsCount;
        this.shift = shift;
    }
}
