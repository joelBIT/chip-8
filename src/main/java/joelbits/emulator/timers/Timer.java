package joelbits.emulator.timers;

public interface Timer<T> {
	void setValue(T value);
	T getValue();
}
