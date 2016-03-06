package joelbits.emu;

public interface Memory {
	short readFromMemory(int location);
	void writeToMemory(short data, int location);
	void clearMemory();
}
