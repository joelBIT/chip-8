package joelbits.emu.output;

/**
 *  A 64x32 display that is basically just an array of pixels that are either in state 1 or 0. Sprites are basically a set 
 *  of bits that indicate a corresponding set of pixel's on/off state. The current state is stored in the display buffer.
 *  The sprites are 8 pixels wide and may be from 1 to 15 pixels in height.
 * 
 * @author rollnyj
 *
 */
public class Display {
	private final int[] displayBuffer = new int[64*32];

	public int readFromDisplayBuffer(int location) {
		return displayBuffer[location];
	}

	public void writeToDisplayBuffer(int data, int location) {
		displayBuffer[location] = data;
	}
	
	public void clearDisplayBuffer() {
		for (int location = 0; location < 2048; location++) {
			displayBuffer[location] = 0x0;
		}
	}
}
