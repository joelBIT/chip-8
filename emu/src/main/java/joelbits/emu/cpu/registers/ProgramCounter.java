package joelbits.emu.cpu.registers;

public final class ProgramCounter<T> implements Register<T> {
	private T programCounter;
	
	@Override
	public T read() {
		return programCounter;
	}

	@Override
	public void write(T data) {
		programCounter = data;
	}
}
