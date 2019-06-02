package joelbits.emulator.timers;

import lombok.Data;

@Data
public final class SoundTimer<T> implements Timer<T> {
	private T value;
}
