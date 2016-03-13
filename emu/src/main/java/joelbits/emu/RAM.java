package joelbits.emu;

/**
 * A very small main memory where the array represents the 4096 memory locations (0x000 - 0xFFF) that is used, where the CHIP-8 interpreter
 * itself will occupy the first 512 bytes (0x000 to 0x1FF) of the memory space. Last 8 bits of each int are used to represent an 
 * unsigned byte.
 * 
 * @author rollnyj
 *
 */
public class RAM implements Memory {
	private int[] memory = new int[1024*4];

	@Override
	public int readFromMemory(int location) {
		return memory[location];
	}

	@Override
	public void writeToMemory(int data, int location) {
		memory[location] = data;
	}
	
	@Override
	public void clearMemory() {
		for (int i = 0; i < 4096; i++) {
			memory[i] = 0x0;
		}
	}
}
