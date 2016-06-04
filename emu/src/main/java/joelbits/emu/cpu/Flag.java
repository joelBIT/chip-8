package joelbits.emu.cpu;

public interface Flag {
	boolean isActive();
	void toggle();
}
