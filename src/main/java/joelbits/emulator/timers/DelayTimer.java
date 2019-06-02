package joelbits.emulator.timers;

import lombok.Data;

@Data
public final class DelayTimer<T> implements Timer<T> {
	private T value;
}
