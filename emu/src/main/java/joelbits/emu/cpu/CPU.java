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
	private final int[] dataRegisters = new int[16];
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
	private final int FIT_8BIT_REGISTER = 0xFF;
	private final int FIT_16BIT_REGISTER = 0xFFFF;
	private int randomNumber;		// For testing purposes
	
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
	
	public void initialize(int programCounter, int instructionRegister, int indexRegister, int delayTimer, int soundTimer, int[] dataRegisters, int[] fontset) {
		this.programCounter = programCounter;
		this.instructionRegister = instructionRegister;
		this.indexRegister = indexRegister;
		this.delayTimer = delayTimer;
		this.soundTimer = soundTimer;
		getDisplay().clearDisplayBuffer();
		getMemory().clearMemory();
		for (int i = 0; i < this.dataRegisters.length; i++) {
			this.dataRegisters[i] = dataRegisters[i];
		}
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
		
		switch(Integer.toHexString(instructionRegister & 0xF000).toUpperCase()) {
			case "0":
				switch(Integer.toHexString(address).toUpperCase()) {
					case "E0":
						getDisplay().clearDisplayBuffer();
						drawFlag = true;
						break;
					case "EE":
						programCounter = stack.pop();
						break;
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
				programCounter += (dataRegisters[registerLocationX] == lowestByte) ? 2 : 0;
				break;
			case "4000":
				programCounter += (dataRegisters[registerLocationX] != lowestByte) ? 2 : 0;
				break;
			case "5000":
				programCounter += (dataRegisters[registerLocationX] == dataRegisters[registerLocationY]) ? 2 : 0;
				break;
			case "6000":
				dataRegisters[registerLocationX] = lowestByte;
				break;
			case "7000":
				dataRegisters[registerLocationX] = (dataRegisters[registerLocationX] + lowestByte) & FIT_8BIT_REGISTER;
				break;
			case "8000":
				switch(Integer.toHexString(nibble).toUpperCase()) {
					case "0":
						dataRegisters[registerLocationX] = dataRegisters[registerLocationY];
						break;
					case "1":
						dataRegisters[registerLocationX] |= dataRegisters[registerLocationY];
						break;
					case "2":
						dataRegisters[registerLocationX] &= dataRegisters[registerLocationY];
						break;
					case "3":
						dataRegisters[registerLocationX] ^= dataRegisters[registerLocationY];
						break;
					case "4":
						int sum = dataRegisters[registerLocationX] + dataRegisters[registerLocationY];
						dataRegisters[0xF] = (sum > FIT_8BIT_REGISTER) ? 1 : 0;
						dataRegisters[registerLocationX] = (sum & lowestByte) & FIT_8BIT_REGISTER;
						break;
					case "5":
						dataRegisters[0xF] = (dataRegisters[registerLocationX] > dataRegisters[registerLocationY]) ? 1 : 0;
						dataRegisters[registerLocationX] = convertToUnsignedInt(dataRegisters[registerLocationX] - dataRegisters[registerLocationY]) & FIT_8BIT_REGISTER;
						break;
					case "6":
						dataRegisters[0xF] = dataRegisters[registerLocationX] & 0x1;
						dataRegisters[registerLocationX] >>= 1;
						break;
					case "7":
						dataRegisters[0xF] = (dataRegisters[registerLocationX] > dataRegisters[registerLocationY]) ? 0 : 1;
						dataRegisters[registerLocationX] = convertToUnsignedInt(dataRegisters[registerLocationY] - dataRegisters[registerLocationX]) & FIT_8BIT_REGISTER;
						break;
					case "E":
						dataRegisters[0xF] = (dataRegisters[registerLocationX] >> 7) & 0x1;
						dataRegisters[registerLocationX] = (dataRegisters[registerLocationX] << 1) & FIT_8BIT_REGISTER;
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & FIT_16BIT_REGISTER) + " at (8) location " + (programCounter-2));
						break;
				}
				break;
			case "9000":
				programCounter += (dataRegisters[registerLocationX] != dataRegisters[registerLocationY]) ? 2 : 0;
				break;
			case "A000":
				indexRegister = address;
				break;
			case "B000":
				programCounter = dataRegisters[0] + address;
				break;
			case "C000":
				randomNumber = new Random().nextInt(FIT_8BIT_REGISTER);
				dataRegisters[registerLocationX] = randomNumber & lowestByte;
				break;
			case "D000":
				dataRegisters[0xF] = 0x0;
				for (int row = 0; row < nibble; row++) {
					int memoryByte = getMemory().readFromMemory(indexRegister + row);
					int coordinateY = dataRegisters[registerLocationY] + row;
					for (int column = 0; column < 8; column++) {
						int coordinateX = dataRegisters[registerLocationX] + column;
						if ((memoryByte & (0x80 >> column)) != 0) {
							if (getDisplay().readFromDisplayBuffer(coordinateX, coordinateY) != 0) {
								dataRegisters[0xF] = 0x1;
							}
							getDisplay().togglePixel(coordinateX, coordinateY);
						}
					}
				}
				drawFlag = true;
				break;
			case "E000":
				switch(Integer.toHexString(lowestByte).toUpperCase()) {
					case "9E":
						programCounter += getKeyboard().getCurrentlyPressedKey() == dataRegisters[registerLocationX] ? 2 : 0;
						break;
					case "A1":
						programCounter += getKeyboard().getCurrentlyPressedKey() != dataRegisters[registerLocationX] ? 2 : 0;
						break;
				}
				break;
			case "F000":
				switch(Integer.toHexString(lowestByte).toUpperCase()) {
					case "7":
						dataRegisters[registerLocationX] = delayTimer;
						break;
					case "A":
						while (getKeyboard().getCurrentlyPressedKey() == 0) {
							;
						}
						dataRegisters[registerLocationX] = getKeyboard().getCurrentlyPressedKey();
						break;
					case "15":
						delayTimer = dataRegisters[registerLocationX];
						break;
					case "18":
						soundTimer = dataRegisters[registerLocationX];
						break;
					case "1E":
						indexRegister = (indexRegister + dataRegisters[registerLocationX]) & FIT_16BIT_REGISTER;
						break;
					case "29":
				 		indexRegister = (dataRegisters[registerLocationX] * 5) & FIT_16BIT_REGISTER;
						break;
					case "33":
				 		getMemory().writeToMemory(dataRegisters[registerLocationX] / 100, indexRegister);
				 		getMemory().writeToMemory((dataRegisters[registerLocationX] % 100) / 10, indexRegister + 1);
				 		getMemory().writeToMemory(dataRegisters[registerLocationX] % 10, indexRegister + 2);
						break;
					case "55":
						for (int i = 0; i <= registerLocationX; i++) {
							getMemory().writeToMemory(dataRegisters[i], indexRegister + i);
						}
						break;
					case "65":
						for (int i = 0; i <= registerLocationX; i++) {
							dataRegisters[i] = getMemory().readFromMemory(indexRegister + i);
						}
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & FIT_16BIT_REGISTER) + " at (f) location " + (programCounter-2));
						break;
				}
				break;
			default:
				System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & FIT_16BIT_REGISTER) + " at (all) location " + (programCounter-2));
				break;
		}
		
		programCounter &= FIT_16BIT_REGISTER;
	}
	
	public int readDataRegister(int registerLocation) {
		return dataRegisters[registerLocation];
	}
	
	public int readIndexRegister() {
		return indexRegister;
	}
	
	public int readSoundTimer() {
		return soundTimer;
	}
	
	public int readDelayTimer() {
		return delayTimer;
	}
	
	public int readProgramCounter() {
		return programCounter;
	}
	
	public int readRandomNumber() {
		return randomNumber;
	}
	
	public int readStackTopValue() {
		return stack.isEmpty() ? -1 : stack.peek();
	}
	
	private int convertToUnsignedInt(int value) {
		return value < 0 ? value + 65536 : value;
	}
}
