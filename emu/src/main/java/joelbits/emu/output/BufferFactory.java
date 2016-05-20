package joelbits.emu.output;

/**
 * 
 * @author rollnyj
 *
 */
public class BufferFactory {
	public static Buffer getDisplayBuffer(int width, int height) {
		return new DisplayBuffer(width, height);
	}
	
	public static Buffer getDirtyBuffer() {
		return new DirtyBuffer();
	}
}
