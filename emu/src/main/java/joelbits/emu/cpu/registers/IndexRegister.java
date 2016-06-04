package joelbits.emu.cpu.registers;

public final class IndexRegister<T> implements Register<T> {
	private T indexRegister;

	@Override
	public T read() {
		return indexRegister;
	}

	@Override
	public void write(T data) {
		indexRegister = data;
	}
}
