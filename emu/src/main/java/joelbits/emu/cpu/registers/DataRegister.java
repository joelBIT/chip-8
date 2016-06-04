package joelbits.emu.cpu.registers;

public final class DataRegister<T> implements Register<T> {
	private T dataRegister;

	@Override
	public T read() {
		return dataRegister;
	}

	@Override
	public void write(T data) {
		dataRegister = data;
	}
}
