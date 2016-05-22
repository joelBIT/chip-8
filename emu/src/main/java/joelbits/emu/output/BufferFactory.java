package joelbits.emu.output;

public class BufferFactory {
	public static Buffer createDisplayBuffer(int width, int height) {
		return new DisplayBuffer(width, height);
	}
	
	public static Buffer createDirtyBuffer() {
		return new DirtyBuffer();
	}
}
