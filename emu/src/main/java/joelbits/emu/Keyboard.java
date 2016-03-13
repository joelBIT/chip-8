package joelbits.emu;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * CHIP-8 is able to detect input from a sixteen key keypad, with each key corresponding to a single unique hexadecimal
 * digit. The key presses of a standard keyboard is mapped to the key presses of a hex keypad.
 * 
 * @author rollnyj
 *
 */
public class Keyboard extends KeyAdapter {
	private int currentlyPressedKey = 0;
	
	private final int[] keyPad = {
			KeyEvent.VK_8, // Key 1
			KeyEvent.VK_4, // Key 2
			KeyEvent.VK_6, // Key 3
			KeyEvent.VK_2, // Key 4
			KeyEvent.VK_Q, // Key 5
			KeyEvent.VK_W, // Key 6
			KeyEvent.VK_E, // Key 7
			KeyEvent.VK_R, // Key 8
			KeyEvent.VK_T, // Key 9
			KeyEvent.VK_Y, // Key A
			KeyEvent.VK_A, // Key B
			KeyEvent.VK_S, // Key C
			KeyEvent.VK_D, // Key D
			KeyEvent.VK_Z, // Key E
			KeyEvent.VK_X, // Key F
		};
	
	public int getCurrentlyPressedKey() {
		return currentlyPressedKey;
	}

    @Override
    public void keyPressed(KeyEvent e) {
    	currentlyPressedKey = mapKeyCodeToChip8Key(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
    	currentlyPressedKey = 0;
    }
    
    public int getKey(int key) {
    	return keyPad[key-1];
    }
    
    private int mapKeyCodeToChip8Key(int keyCode) {
		for (int i = 0; i < keyPad.length; i++) {
			if (keyPad[i] == keyCode) {
				return i + 1;
			}
		}
		return 0;
	}
}
