package joelbits.emulator.units;

import joelbits.emulator.memory.Memory;

/**
 * Memory Management Unit. Handles tasks related to memory.
 */
public final class MMU {
    private final Memory primaryMemory;

    public MMU(Memory primaryMemory) {
        this.primaryMemory = primaryMemory;
    }

    public void clearRAM() {
        primaryMemory.clear();
    }

    public void writeRAM(int[] data) {
        for (int i = 0; i < data.length; i++) {
            primaryMemory.write(i, data[i]);
        }
    }

    public void writeRAM(int location, int data) {
        primaryMemory.write(location, data);
    }

    public int[] ram() {
        int[] ram = new int[primaryMemory.size()];
        for (int i = 0; i < primaryMemory.size(); i++) {
            ram[i] = primaryMemory.read(i);
        }
        return ram;
    }

    public int readRAM(int location) {
        return primaryMemory.read(location);
    }
}
