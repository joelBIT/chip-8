package joelbits.emu;

/**
 *  A 64x32 display that is basically just an array of pixels that are either in state 1 or 0. Sprites are basically a set 
 *  of bits that indicate a corresponding set of pixel's on/off state.
 * 
 * @author rollnyj
 *
 */
public class Display {
	private final short[] displayBuffer = new short[64*32];
	
	public short readFromDisplayBuffer(int location) {
		return displayBuffer[location];
	}

	public void writeToDisplayBuffer(short data, int location) {
		displayBuffer[location] = data;
	}
	
	public void clearDisplayBuffer() {
		for (int i = 0; i < 2048; i++) {
			displayBuffer[i] = 0x0;
		}
	}
}
