package joelbits.emu.output;

public interface Buffer {
	int read(int location);
	void write(int data, int location);
	void clear();
	int size();
}
