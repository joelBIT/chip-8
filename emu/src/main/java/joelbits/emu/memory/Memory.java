package joelbits.emu.memory;

public interface Memory {
	int read(int location);
	void write(int data, int location);
	void clear();
}
