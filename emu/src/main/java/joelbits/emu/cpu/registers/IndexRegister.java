package joelbits.emu.cpu.registers;

public final class IndexRegister<T> implements Register<T> {
	private static final Register<Integer> indexRegister = new IndexRegister<Integer>();
	private T data;
	
	private IndexRegister() {
		
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
		return indexRegister;
	}
}
