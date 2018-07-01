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

    public void clearPrimaryMemory() {
        primaryMemory.clear();
    }

    public void writePrimaryMemory(int[] data) {
        for (int i = 0; i < data.length; i++) {
            primaryMemory.write(i, data[i]);
        }
    }

    public void writePrimaryMemory(int location, int data) {
        primaryMemory.write(location, data);
    }

    public int[] primaryMemory() {
        int[] ram = new int[primaryMemory.size()];
        for (int i = 0; i < primaryMemory.size(); i++) {
            ram[i] = primaryMemory.read(i);
        }
        return ram;
    }

    public int readPrimaryMemory(int location) {
        return primaryMemory.read(location);
    }
}
