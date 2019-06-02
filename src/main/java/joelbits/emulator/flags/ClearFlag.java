package joelbits.emulator.flags;

import lombok.Getter;

public final class ClearFlag implements Flag {
	@Getter private boolean active;

	@Override
	public void toggle() {
		active = !active;
	}
}
