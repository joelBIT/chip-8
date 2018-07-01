package joelbits.emulator.output;

public abstract class Screen<T> {
	private final T width;
	private final T height;
	private final T pixelSize;
	
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

	public abstract void fill(double x, double y);
	public abstract void clear(double x, double y);
	public abstract void clearAll(int bufferSize);
}
