package joelbits.emu.cpu.registers;

public final class DataRegister<T> implements Register<T> {
	private T data;

	@Override
	public T read() {
		return data;
	}

	@Override
	public void write(T data) {
		this.data = data;
	}
}
