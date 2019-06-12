package joelbits.emulator;

/**
 * Represents a Chip8 program.
 */
public class Program {
    private final byte[] data;

    Program(byte[] data) {
        this.data = data;
    }

    public int size() {
        return data.length;
    }

    public byte data(int location) {
        return data[location];
    }
}
