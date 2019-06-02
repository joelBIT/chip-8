package joelbits.emulator.output;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class Screen<T> {
	private final T width;
	private final T height;
	private final T pixelSize;

	public abstract void fill(double x, double y);
	public abstract void clear(double x, double y);
	public abstract void clearAll(int bufferSize);
}
