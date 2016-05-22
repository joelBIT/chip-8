package joelbits.emu.cpu;

public interface Timer<T> {
	public void setValue(T value);
	public T currentValue();
}
