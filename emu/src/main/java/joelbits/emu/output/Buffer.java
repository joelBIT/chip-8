package joelbits.emu.output;

public interface Buffer {
	int read(int location);
	void write(int location, int data);
	void clear();
	int size();
}
