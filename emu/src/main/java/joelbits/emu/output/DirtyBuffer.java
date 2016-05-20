package joelbits.emu.output;

import java.util.ArrayList;
import java.util.List;

/**
 *  A dirty buffer is used to keep track of which pixel locations that has been changed. That way only the affected
 *  pixels are re-rendered.
 * 
 * @author rollnyj
 *
 */
public class DirtyBuffer implements Buffer {
	private final List<Integer> dirtyBuffer = new ArrayList<>();
	
	/**
	 * It does not matter which element is removed because all of this buffer's elements should be drawn on the display (FIFO).
	 */
	@Override
	public int read(int index) {
		return dirtyBuffer.size() > 0 ? dirtyBuffer.remove(0) : -1;
	}

	/**
	 * No data is written to buffer, only memory index of affected pixels are of interest.
	 */
	@Override
	public void write(int data, int index) {
		dirtyBuffer.add(index);
	}

	@Override
	public void clear() {
		dirtyBuffer.clear();
	}

	@Override
	public int size() {
		return dirtyBuffer.size();
	}
}
