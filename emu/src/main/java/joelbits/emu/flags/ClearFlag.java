package joelbits.emu.flags;

public final class ClearFlag implements Flag {
	private boolean active;

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void toggle() {
		active = !active;
	}
}
