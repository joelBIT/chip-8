package joelbits.emu.output;

import java.util.ArrayList;
import java.util.List;

/**
 *  A 64x32 display that is basically just an array of pixels that are either in state 1 or 0. The current state is
 *  stored in the display buffer.
 *  
 *  A dirty buffer is used to keep track of which pixel locations that has been written to. That way only the affected
 *  pixels are re-rendered.
 * 
 * @author rollnyj
 *
 */
public class Display {
	private final int[] displayBuffer = new int[SCREEN_WIDTH * SCREEN_HEIGHT];
	private final List<Integer> dirtyBuffer = new ArrayList<>();
	public static final int SCREEN_WIDTH = 64;
	public static final int SCREEN_HEIGHT = 32;
	
	public int readFromDisplayBuffer(int coordinateX, int coordinateY) {
		return displayBuffer[convertToIndex(wrapCoordinateX(coordinateX), wrapCoordinateY(coordinateY))];
	}
	
	public int readFromDisplayBuffer(int index) {
		return displayBuffer[wrapIndex(index)];
	}
	
	public void togglePixel(int coordinateX, int coordinateY) {
		displayBuffer[convertToIndex(wrapCoordinateX(coordinateX), wrapCoordinateY(coordinateY))] ^= 1;
	}

	public void writeToDisplayBuffer(int data, int index) {
		displayBuffer[index] = data;
	}

	public void clearDisplayBuffer() {
		for (int index = 0; index < displayBuffer.length; index++) {
			displayBuffer[index] = 0x0;
		}
	}
	
	public void clearDirtyBuffer() {
		for (int index = 0; index < displayBuffer.length; index++) {
			dirtyBuffer.add(index);
		}
	}
	
	public void addDirtyLocation(int coordinateX, int coordinateY) {
		dirtyBuffer.add(convertToIndex(wrapCoordinateX(coordinateX), wrapCoordinateY(coordinateY)));
	}
	
	public int removeDirtyLocation() {
		return dirtyBuffer.size() > 0 ? dirtyBuffer.remove(0) : -1;
	}
	
	public int readFromDirtyBuffer(int coordinateX, int coordinateY) {
		return dirtyBuffer.get(convertToIndex(wrapCoordinateX(coordinateX), wrapCoordinateY(coordinateY)));
	}
	
	public int dirtyBufferSize() {
		return dirtyBuffer.size();
	}
	
	private int wrapCoordinateX(int coordinateX) {
		return coordinateX >= SCREEN_WIDTH ? coordinateX % SCREEN_WIDTH : coordinateX;
	}
	
	private int wrapCoordinateY(int coordinateY) {
		return coordinateY >= SCREEN_HEIGHT ? coordinateY % SCREEN_HEIGHT : coordinateY;
	}
	
	private int wrapIndex(int index) {
		return index >= displayBuffer.length ? index % displayBuffer.length : index;
	}
	
	private int convertToIndex(int coordinateX, int coordinateY) {
		return coordinateX + (coordinateY * SCREEN_WIDTH);
	}
}
