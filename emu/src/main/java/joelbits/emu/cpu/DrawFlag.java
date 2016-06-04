package joelbits.emu.cpu;

public class DrawFlag implements Flag {
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
