package joelbits.emu.cpu.registers;

public final class ProgramCounter<T> implements Register<T> {
	private static final Register<Integer> programCounter = new ProgramCounter<Integer>();
	private T data;
	
	private ProgramCounter() {
		
	}
	
	@Override
	public T read() {
		return data;
	}

	@Override
	public void write(T data) {
		this.data = data;
	}
	
	public static final Register<Integer> getInstance() {
		return programCounter;
	}
}
