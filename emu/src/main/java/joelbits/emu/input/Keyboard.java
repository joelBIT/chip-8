package joelbits.emu.input;

import javafx.scene.input.KeyCode;

/**
 * CHIP-8 is able to detect input from a sixteen key keypad, with each key corresponding to a single unique hexadecimal
 * digit. The key presses of a standard keyboard is mapped to the key presses of a hex keypad.
 * 
 * @author rollnyj
 *
 */
public class Keyboard {
	private int currentlyPressedKey;
	private final char[] keyPad = {'8', '4', '6', '2', 'Q', 'W', 'E', 'R', 'T', 'Y', 'A', 'S', 'D', 'Z', 'X'};
	
	public int getCurrentlyPressedKey() {
		return currentlyPressedKey;
	}

    public void keyPressed(KeyCode keyCode) {
    	currentlyPressedKey = mapKeyCodeToChip8Key(keyCode);
    }

    public void keyReleased() {
    	currentlyPressedKey = 0;
    }
    
    public char getKey(int key) {
    	return keyPad[key-1];
    }
    
    private int mapKeyCodeToChip8Key(KeyCode keyCode) {
		for (int i = 0; i < keyPad.length; i++) {
			if (keyPad[i] == keyCode.getName().charAt(0)) {
				return i + 1;
			}
		}
		return 0;
	}
}
