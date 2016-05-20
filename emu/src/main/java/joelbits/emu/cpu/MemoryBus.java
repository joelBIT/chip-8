package joelbits.emu.cpu;

import joelbits.emu.memory.Memory;
import joelbits.emu.memory.RAM;

/**
 * Used by the CPU to access the main memory.
 * 
 * @author rollnyj
 *
 */
public class MemoryBus {
	private Memory primaryMemory = new RAM();

	public Memory getPrimaryMemory() {
		return primaryMemory;
	}
}
