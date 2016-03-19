package joelbits.emu.memory;

public interface Memory {
	int readFromMemory(int location);
	void writeToMemory(int data, int location);
	void clearMemory();
}
