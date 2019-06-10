package joelbits.emulator;

public class Program {
    private final byte[] data;

    public Program(byte[] data) {
        this.data = data;
    }

    public int size() {
        return data.length;
    }

    public byte data(int location) {
        return data[location];
    }
}
