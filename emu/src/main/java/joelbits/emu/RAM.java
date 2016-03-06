package joelbits.emu;

/**
 * A very small main memory where the array represents the 4096 memory locations (0x000 - 0xFFF) that is used, where the CHIP-8 interpreter
 * itself will occupy the first 512 bytes of the memory space. Last 8 bits of each element are used to represent an unsigned byte.
 * 
 * @author rollnyj
 *
 */
public class RAM implements Memory {
	private short[] memory = new short[1024*4];

	@Override
	public short readFromMemory(int location) {
		return memory[location];
	}

	@Override
	public void writeToMemory(short data, int location) {
		memory[location] = data;
	}
	
	@Override
	public void clearMemory() {
		for (int i = 0; i < 4096; i++) {
			memory[i] = 0;
		}
	}

}
