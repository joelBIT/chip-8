package joelbits.emu.cpu;

import java.util.Random;
import java.util.Stack;

import joelbits.emu.input.Keyboard;
import joelbits.emu.memory.Memory;
import joelbits.emu.output.Display;

/**
 * There are 16 data registers named from V0 to VF. The carry flag (VF) is set to 1 if any screen pixels are flipped from set 
 * to unset when a sprite is drawn and set to 0 otherwise. Last 8 bits of each register are used to represent an unsigned byte.
 * In this CPU implementation components like the control unit (CU) and the arithmetic-logic unit (ALU) are abstracted away.
 * 
 * @author rollnyj
 *
 */
public class CPU {
	private final MemoryBus memoryBus = new MemoryBus();
	private final ExpansionBus expansionBus = new ExpansionBus();
	private final Stack<Integer> stack = new Stack<Integer>();
	private final int[] registers = new int[16];
	private int instructionRegister;
	private int indexRegister;
	private int programCounter;
	private int delayTimer;
	private int soundTimer;
	private boolean drawFlag;
	private int registerLocationX;
	private int registerLocationY;
	private int nibble;
	private int address;
	private int lowestByte;
	
	public Keyboard getKeyboard() {
		return expansionBus.getKeyboard();
	}
	
	public Display getDisplay() {
		return expansionBus.getDisplay();
	}
	
	public Memory getMemory() {
		return memoryBus.getPrimaryMemory();
	}
	
	public boolean isDrawFlag() {
		return drawFlag;
	}
	
	public void toggleDrawFlag() {
		drawFlag = !drawFlag;
	}
	
	public void initialize(int programCounter, int instructionRegister, int indexRegister, int delayTimer, int soundTimer, int[] fontset) {
		this.programCounter = programCounter;
		this.instructionRegister = instructionRegister;
		this.indexRegister = indexRegister;
		this.delayTimer = delayTimer;
		this.soundTimer = soundTimer;
		getDisplay().clearDisplayBuffer();
		getMemory().clearMemory();
		for (int i = 0; i < fontset.length; i++) {
			getMemory().writeToMemory(fontset[i], i);
		}
	}
	
	public void loadROM(byte[] ROM, int startLocation) {
		for (int i = 0, location = startLocation; i < ROM.length; i++, location++) {
			getMemory().writeToMemory(Byte.toUnsignedInt(ROM[i]), location);
		}
	}

	public void nextInstructionCycle() {
		instructionRegister = getMemory().readFromMemory(programCounter) << 8 | getMemory().readFromMemory(programCounter+1);
		programCounter += 2;
		
		registerLocationX = (instructionRegister & 0x0F00) >> 8;
		registerLocationY = (instructionRegister & 0x00F0) >> 4;
		nibble = instructionRegister & 0x000F;
		address = instructionRegister & 0x0FFF;
		lowestByte = instructionRegister & 0x00FF;
		
		switch(Integer.toHexString(instructionRegister & 0xF000)) {
			case "0":
				if (Integer.toHexString(instructionRegister & 0xFFFF) == "00E0") {
					getDisplay().clearDisplayBuffer();
					drawFlag = true;
				} else if (Integer.toHexString(instructionRegister & 0xFFFF) == "00EE") {
					programCounter = stack.pop();
				}
				break;
			case "1000":
				programCounter = address;
				break;
			case "2000":
				stack.push(programCounter);
				programCounter = address;
				break;
			case "3000":
				programCounter += (registers[registerLocationX] == lowestByte) ? 2 : 0;
				break;
			case "4000":
				programCounter += (registers[registerLocationX] != lowestByte) ? 2 : 0;
				break;
			case "5000":
				programCounter += (registers[registerLocationX] == registers[registerLocationY]) ? 2 : 0;
				break;
			case "6000":
				registers[registerLocationX] = lowestByte;
				break;
			case "7000":
				registers[registerLocationX] += lowestByte;
				break;
			case "8000":
				switch(Integer.toHexString(nibble)) {
					case "0":
						registers[registerLocationX] = registers[registerLocationY];
						break;
					case "1":
						registers[registerLocationX] |= registers[registerLocationY];
						break;
					case "2":
						registers[registerLocationX] &= registers[registerLocationY];
						break;
					case "3":
						registers[registerLocationX] ^= registers[registerLocationY];
						break;
					case "4":
						int sum = registers[registerLocationX] + registers[registerLocationY];
						registers[0xF] = (sum > 0xFF) ? 1 : 0;
						registers[registerLocationX] = sum & lowestByte;
						break;
					case "5":
						registers[0xF] = (registers[registerLocationX] > registers[registerLocationY]) ? 1 : 0;
						registers[registerLocationX] -= registers[registerLocationY];
						break;
					case "6":
						registers[0xF] = registers[registerLocationX] & 0x1;
						registers[registerLocationX] >>= 1;
						break;
					case "7":
						registers[0xF] = (registers[registerLocationX] > registers[registerLocationY]) ? 0 : 1;
						registers[registerLocationX] = registers[registerLocationY] - registers[registerLocationX];
						break;
					case "e":
						registers[0xF] = (registers[registerLocationX] >> 7) & 0x1;
						registers[registerLocationX] <<= 1;
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & 0xFFFF) + " at (8) location " + programCounter);
						break;
				}
				break;
			case "9000":
				programCounter += (registers[registerLocationX] != registers[registerLocationY]) ? 2 : 0;
				break;
			case "a000":
				indexRegister = address;
				break;
			case "b000":
				programCounter = registers[0] + address;
				break;
			case "c000":
				registers[registerLocationX] = (new Random().nextInt(0xFF + 1)) & lowestByte;
				break;
			case "d000":
				registers[0xF] = 0;
				for (int row = 0; row < nibble; row++) {
					int memoryByte = getMemory().readFromMemory(indexRegister + row);
					int coordinateY = registers[registerLocationY] + row;
					for (int column = 0; column < 8; column++) {
						int coordinateX = registers[registerLocationX] + column;
						if ((memoryByte & (0x80 >> column)) != 0) {
							if (getDisplay().readFromDisplayBuffer(coordinateX, coordinateY) != 0) {
								registers[0xF] = 1;
							}
							getDisplay().togglePixel(coordinateX, coordinateY);
						}
					}
				}
				drawFlag = true;
				break;
			case "e000":
				if (nibble == 1) {
					programCounter += getKeyboard().getCurrentlyPressedKey() != getKeyboard().getKey(registers[registerLocationX]) ? 2 : 0;
				} else {
					programCounter += getKeyboard().getCurrentlyPressedKey() == getKeyboard().getKey(registers[registerLocationX]) ? 2 : 0;
				}
				break;
			case "f000":
				switch(Integer.toHexString(lowestByte)) {
					case "15":
						delayTimer = registers[registerLocationX];
						break;
					case "18":
						soundTimer = registers[registerLocationX];
						break;
					case "7":
						registers[registerLocationX] = delayTimer;
						break;
					case "a":
						while (getKeyboard().getCurrentlyPressedKey() == 0) {
							;
						}
						registers[registerLocationX] = getKeyboard().getCurrentlyPressedKey();
						break;
					case "1e":
						indexRegister = (indexRegister + registers[registerLocationX]) & 0xFFF;
						break;
					case "29":
				 		indexRegister = registers[registerLocationX] * 5;
						break;
					case "33":
				 		getMemory().writeToMemory(registers[registerLocationX] / 100, indexRegister);
				 		getMemory().writeToMemory((registers[registerLocationX] % 100) / 10, indexRegister + 1);
				 		getMemory().writeToMemory(registers[registerLocationX] % 10, indexRegister + 2);
						break;
					case "55":
						for (int i = 0; i <= registerLocationX; i++) {
							getMemory().writeToMemory(registers[i], indexRegister + i);
						}
						break;
					case "65":
						for (int i = 0; i <= registerLocationX; i++) {
							registers[i] = getMemory().readFromMemory(indexRegister + i);
						}
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & 0xFFFF) + " at (f) location " + programCounter);
						break;
				}
				break;
			default:
				System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & 0xFFFF) + " at (all) location " + programCounter);
				break;
		}
		
		programCounter &= 0xFFF;

		if (delayTimer > 0) {
			delayTimer -= 1;
		}
		
		if (soundTimer > 0) {
			soundTimer -= 1;
		}
	}
}
