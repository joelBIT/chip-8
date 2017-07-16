package joelbits.emu.memory;

import joelbits.emu.memory.Memory;

/**
 * A memory where the array represents the 4096 memory locations (0x000 - 0xFFF) that is used by the CHIP-8, where the 
 * CHIP-8 interpreter itself will occupy the first 512 bytes (0x000 to 0x1FF) of the memory space.
 *
 */
public final class RAM implements Memory {
	private final int[] memory;
	
	public RAM() {
		memory = new int[4096];
	}

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
