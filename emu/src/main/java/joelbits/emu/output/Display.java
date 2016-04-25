package joelbits.emu.output;

/**
 *  A 64x32 display that is basically just an array of pixels that are either in state 1 or 0. The current state is
 *  stored in the display buffer.
 * 
 * @author rollnyj
 *
 */
public class Display {
	private final int[] displayBuffer = new int[SCREEN_WIDTH*SCREEN_HEIGHT];
	public static final int SCREEN_WIDTH = 64;
	public static final int SCREEN_HEIGHT = 32;
	
	public int readFromDisplayBuffer(int coordinateX, int coordinateY) {
		return displayBuffer[coordinateX + (coordinateY*SCREEN_WIDTH)];
	}
	
	public void togglePixel(int coordinateX, int coordinateY) {
		displayBuffer[coordinateX + (coordinateY*SCREEN_WIDTH)] ^= 1;
	}

	public void writeToDisplayBuffer(int data, int location) {
		displayBuffer[location] = data;
	}

	public void clearDisplayBuffer() {
		for (int location = 0; location < displayBuffer.length; location++) {
			displayBuffer[location] = 0x0;
		}
	}
}
