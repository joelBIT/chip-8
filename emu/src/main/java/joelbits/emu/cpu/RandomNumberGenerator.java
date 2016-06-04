package joelbits.emu.cpu;

import java.util.Random;

public class RandomNumberGenerator {
	private int value;
	private Random random = new Random();
	
	public void generate(int bound) {
		value = random.nextInt(bound);
	}
	
	public int value() {
		return value;
	}
}
