package joelbits.emu;

import java.util.Stack;

import joelbits.emu.cpu.CPU;

/**
 * Chip-8 emulator.
 * 
 * Last 8 bits of each short are used to represent an unsigned byte, while the last 16 bits of each int are used as an unsigned short. 
 * 
 * @author rollnyj
 *
 */
public class Chip8 {
	private Stack stack = new Stack();
	private CPU cpu = new CPU();
	
	public static void main(String[] args) {
		Chip8 chip8 = new Chip8();
		
		chip8.initialize();
	}
	
	public void initialize() {
		cpu.setProgramCounter(0x200);
		cpu.setInstructionRegister(0x000);
	}

}
