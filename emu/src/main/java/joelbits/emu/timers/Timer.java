package joelbits.emu.timers;

public interface Timer<T> {
	public void setValue(T value);
	public T currentValue();
}
