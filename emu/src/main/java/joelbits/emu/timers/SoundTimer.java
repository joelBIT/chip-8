package joelbits.emu.timers;

public class SoundTimer<T> implements Timer<T> {
	private T value;

	@Override
	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public T currentValue() {
		return value;
	}
}