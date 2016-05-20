package joelbits.emu.output;

/**
 *  A display that is basically just an array of pixels that are either in state 0 or 1. The current screen state is
 *  stored in the display buffer.
 * 
 * @author rollnyj
 *
 */
public class DisplayBuffer implements Buffer {
	private final int[] displayBuffer;
	
	public DisplayBuffer(int width, int height) {
		displayBuffer = new int[width*height];
	}
	
	@Override
	public int read(int index) {
		return displayBuffer[wrapIndex(index)];
	}
	
	private int wrapIndex(int index) {
		return index >= displayBuffer.length ? index % displayBuffer.length : index;
	}

	@Override
	public void write(int data, int index) {
		displayBuffer[wrapIndex(index)] = data;
	}

	@Override
	public void clear() {
		for (int i = 0; i < displayBuffer.length; i++) {
			displayBuffer[i] = 0;
		}
	}
	
	@Override
	public int size() {
		return displayBuffer.length;
	}
}
