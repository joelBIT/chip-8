package joelbits.emulator.memory;

public final class BufferFactory {
	public static Memory createDisplayBuffer(int width, int height) {
		return new DisplayBuffer(width, height);
	}
	
	public static Memory createDirtyBuffer() {
		return new DirtyBuffer();
	}
}
