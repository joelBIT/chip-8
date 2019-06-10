package joelbits.emulator;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Program {
    private final byte[] data;

    public Program(byte[] data) throws IOException {
        this.data = data;
    }

    public int length() {
        return data.length;
    }

    public byte data(int location) {
        return data[location];
    }
}
