package joelbits.emulator.memory;

public interface Memory {
	int read(int location);
	void write(int location, int data);
	void clear();
	int size();
}
