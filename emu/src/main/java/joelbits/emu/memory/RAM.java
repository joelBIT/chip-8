package joelbits.emu.memory;

import joelbits.emu.memory.Memory;

/**
 * A very small main memory where the array represents the 4096 memory locations (0x000 - 0xFFF) that is used, where the CHIP-8 interpreter
 * itself will occupy the first 512 bytes (0x000 to 0x1FF) of the memory space. Last 8 bits of each int are used to represent an 
 * unsigned byte.
 *
 */
public class RAM implements Memory {
	private final int MEMORY_LOCATIONS = 4096;
	private int[] memory = new int[MEMORY_LOCATIONS];

	@Override
	public int read(int index) {
		return memory[index];
	}

	@Override
	public void write(int index, int data) {
		memory[index] = data;
	}
	
	@Override
	public void clear() {
		for (int i = 0; i < memory.length; i++) {
			memory[i] = 0x0;
		}
	}

	@Override
	public int size() {
		return memory.length;
	}
}
