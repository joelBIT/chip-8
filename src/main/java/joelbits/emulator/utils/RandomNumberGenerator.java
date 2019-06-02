package joelbits.emulator.utils;

import lombok.Getter;

import java.util.Random;

public class RandomNumberGenerator {
	@Getter private int value;
	private final Random random = new Random();
	
	public void generate(int bound) {
		value = random.nextInt(bound);
	}
}
