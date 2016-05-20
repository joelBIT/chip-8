package joelbits.emu;

/**
 * The screen for the chip-8 interpreter.
 * 
 * @author rollnyj
 *
 * @param <T>
 */
public class Screen<T> {
	private T width;
	private T height;
	private T pixelSize;
	
	public Screen(T width, T height, T pixelSize) {
		this.width = width;
		this.height = height;
		this.pixelSize = pixelSize;
	}
	
	public T width() {
		return width;
	}
	
	public T height() {
		return height;
	}
	
	public T pixelSize() {
		return pixelSize;
	}
}
