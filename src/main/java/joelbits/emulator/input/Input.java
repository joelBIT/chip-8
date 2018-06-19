package joelbits.emulator.input;

public interface Input<S, T> {
	S currentlyPressed();
	void press(T pressed);
	void releasePressed();
}
