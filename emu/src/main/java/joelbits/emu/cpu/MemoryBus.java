package joelbits.emu.cpu;

import joelbits.emu.memory.Memory;
import joelbits.emu.memory.RAM;

public class MemoryBus {
	private Memory primaryMemory = new RAM();

	public Memory getPrimaryMemory() {
		return primaryMemory;
	}
}
