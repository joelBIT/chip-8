package joelbits.emu.memory;

/**
 * The current display state is stored in this buffer. Value 1 means that a pixel is visible on the display at the corresponding coordinates,
 * while value 0 means that there is no visible pixel at the corresponding coordinates. Here visible means that the drawn pixel has a color
 * different from the background color.
 *
 */
public final class DisplayBuffer implements Memory {
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
	public void write(int index, int data) {
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
