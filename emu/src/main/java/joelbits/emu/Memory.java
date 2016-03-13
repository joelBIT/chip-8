package joelbits.emu;

public interface Memory {
	int readFromMemory(int location);
	void writeToMemory(int data, int location);
	void clearMemory();
}
