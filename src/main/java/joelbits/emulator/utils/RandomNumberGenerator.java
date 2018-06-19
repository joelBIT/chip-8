package joelbits.emulator.utils;

import java.util.Random;

public class RandomNumberGenerator {
	private int value;
	private final Random random = new Random();
	
	public void generate(int bound) {
		value = random.nextInt(bound);
	}
	
	public int value() {
		return value;
	}
}
