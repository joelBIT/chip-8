package joelbits.emulator.timers;

public final class SoundTimer<T> implements Timer<T> {
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
