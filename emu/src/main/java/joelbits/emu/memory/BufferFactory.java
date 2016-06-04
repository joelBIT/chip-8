package joelbits.emu.memory;

public class BufferFactory {
	public static Memory createDisplayBuffer(int width, int height) {
		return new DisplayBuffer(width, height);
	}
	
	public static Memory createDirtyBuffer() {
		return new DirtyBuffer();
	}
}
