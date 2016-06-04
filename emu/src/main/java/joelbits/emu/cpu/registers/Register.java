package joelbits.emu.cpu.registers;

public interface Register<T> {
	T read();
	void write(T data);
}
