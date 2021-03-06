package joelbits.emulator.memory;

import java.util.ArrayList;
import java.util.List;

/**
 *  A dirty buffer is used to keep track of which pixels (their locations) that has been changed. That way only the affected
 *  pixels have to be re-rendered.
 *
 */
public final class DirtyBuffer implements Memory {
	private final List<Integer> dirtyBuffer = new ArrayList<>();
	
	/**
	 * It does not matter which element is removed because all of this buffer's elements should be drawn on the display at the same time (FIFO).
	 */
	@Override
	public int read(int index) {
		return dirtyBuffer.size() > 0 ? dirtyBuffer.remove(0) : -1;
	}

	/**
	 * No data is written to buffer, only memory index of affected pixels are of interest.
	 */
	@Override
	public void write(int index, int data) {
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
