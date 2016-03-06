package joelbits.emu.cpu;

import joelbits.emu.Memory;
import joelbits.emu.RAM;

/**
 * Used by the CPU to access the main memory.
 * 
 * @author rollnyj
 *
 */
public class MemoryBus {
	private final Memory primaryMemory = new RAM();
	
	public Memory getPrimaryMemory() {
		return primaryMemory;
	}
}
