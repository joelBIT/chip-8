package joelbits.emulator.input;

import javafx.scene.input.KeyCode;

import java.util.Objects;

/**
 * CHIP-8 is able to detect input from a sixteen key keypad, with each key corresponding to a single unique hexadecimal
 * digit. The key presses of a standard keyboard is mapped to the key presses of a hex keypad.
 *
 */
public class Keyboard implements Input<Integer, KeyCode> {
	private int currentlyPressedKey;
	private final char[] keyPad = {'8', '4', '6', '2', 'Q', 'W', 'E', 'R', 'T', 'Y', 'A', 'S', 'D', 'Z', 'X', 'C'};
	
	@Override
	public Integer currentlyPressed() {
		return currentlyPressedKey;
	}

	@Override
    public void press(KeyCode keyCode) {
    	currentlyPressedKey = mapKeyCodeToChip8Key(keyCode);
    }
    
    private int mapKeyCodeToChip8Key(KeyCode keyCode) {
		for (int i = 0; i < keyPad.length; i++) {
			if (Objects.equals(keyPad[i], keyCode.getName().charAt(0))) {
				return i + 1;
			}
		}
		return 0;
	}

    @Override
    public void releasePressed() {
    	currentlyPressedKey = 0;
    }
}
