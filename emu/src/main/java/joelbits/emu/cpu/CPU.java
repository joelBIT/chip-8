package joelbits.emu.cpu;

import java.util.Random;
import java.util.Stack;

import joelbits.emu.Screen;
import joelbits.emu.input.Keyboard;
import joelbits.emu.memory.Memory;
import joelbits.emu.output.Buffer;
import joelbits.emu.output.BufferFactory;
import joelbits.emu.output.Sound;

/**
 * There are 16 data registers named from V0 to VF. The carry flag (VF) is set to 1 if any screen pixels are flipped from set 
 * to unset when a sprite is drawn and set to 0 otherwise. Last 8 bits of each register are used to represent an unsigned byte.
 * In this CPU implementation components like the control unit (CU) and the arithmetic-logic unit (ALU) are abstracted away.
 * 
 * @author rollnyj
 *
 */
public class CPU {
	private MemoryBus memoryBus = new MemoryBus();
	private final ExpansionBus expansionBus = new ExpansionBus();
	private final Buffer displayBuffer = BufferFactory.getDisplayBuffer(getScreen().width(), getScreen().height());
	private final Buffer dirtyBuffer = BufferFactory.getDirtyBuffer();
	private final Stack<Integer> stack = new Stack<Integer>();
	private final int[] dataRegisters = new int[16];
	private int instructionRegister;
	private int indexRegister;
	private int programCounter;
	private int delayTimer;
	private int soundTimer;
	private boolean drawFlag;
	private boolean clearFlag;
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
	
	public Buffer getDisplayBuffer() {
		return displayBuffer;
	}
	
	public Buffer getDirtyBuffer() {
		return dirtyBuffer;
	}
	
	public Sound getSound() {
		return expansionBus.getSound();
	}
	
	public Screen<Integer> getScreen() {
		return expansionBus.getScreen();
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
	
	public boolean isClearFlag() {
		return clearFlag;
	}
	
	public void toggleClearFlag() {
		clearFlag = !clearFlag;
	}
	
	public synchronized void decrementDelayTimer() {
		if (delayTimer > 0) {
			delayTimer--;
		}
	}
	
	public synchronized void decrementSoundTimer() {
		soundTimer--;
		if (soundTimer == 0) {
			getSound().stop();
		}
	}
	
	public void initialize(int programCounter, int instructionRegister, int indexRegister, int delayTimer, int soundTimer, int[] dataRegisters, int[] fontset) {
		this.programCounter = programCounter;
		this.instructionRegister = instructionRegister;
		this.indexRegister = indexRegister;
		this.delayTimer = delayTimer;
		this.soundTimer = soundTimer;
		getMemory().clear();
		dirtyBuffer.clear();
		displayBuffer.clear();
		getMemory().clear();
		for (int i = 0; i < this.dataRegisters.length; i++) {
			this.dataRegisters[i] = dataRegisters[i];
		}
		for (int i = 0; i < fontset.length; i++) {
			getMemory().write(fontset[i], i);
		}
	}
	
	public void loadROM(byte[] ROM, int startLocation) {
		for (int i = 0, location = startLocation; i < ROM.length; i++, location++) {
			getMemory().write(Byte.toUnsignedInt(ROM[i]), location);
		}
	}
	
	public void nextInstructionCycle() {
		instructionRegister = getMemory().read(programCounter) << 8 | getMemory().read(programCounter+1);
		
		registerLocationX = (instructionRegister & 0x0F00) >> 8;
		registerLocationY = (instructionRegister & 0x00F0) >> 4;
		nibble = instructionRegister & 0x000F;
		address = instructionRegister & 0x0FFF;
		lowestByte = instructionRegister & 0x00FF;
		
		switch(Integer.toHexString(instructionRegister & 0xF000).toUpperCase()) {
			case "0":
				switch(Integer.toHexString(address).toUpperCase()) {
					case "E0":
						displayBuffer.clear();
						dirtyBuffer.clear();
						clearFlag = true;
						programCounter += 2;
						break;
					case "EE":
						programCounter = stack.pop();
						programCounter += 2;
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & FIT_16BIT_REGISTER) + " at (0) location " + programCounter);
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
				programCounter += (dataRegisters[registerLocationX] == lowestByte) ? 4 : 2;
				break;
			case "4000":
				programCounter += (dataRegisters[registerLocationX] != lowestByte) ? 4 : 2;
				break;
			case "5000":
				programCounter += (dataRegisters[registerLocationX] == dataRegisters[registerLocationY]) ? 4 : 2;
				break;
			case "6000":
				dataRegisters[registerLocationX] = lowestByte;
				programCounter += 2;
				break;
			case "7000":
				dataRegisters[registerLocationX] = (dataRegisters[registerLocationX] + lowestByte) & FIT_8BIT_REGISTER;
				programCounter += 2;
				break;
			case "8000":
				switch(Integer.toHexString(nibble).toUpperCase()) {
					case "0":
						dataRegisters[registerLocationX] = dataRegisters[registerLocationY];
						programCounter += 2;
						break;
					case "1":
						dataRegisters[registerLocationX] |= dataRegisters[registerLocationY];
						programCounter += 2;
						break;
					case "2":
						dataRegisters[registerLocationX] &= dataRegisters[registerLocationY];
						programCounter += 2;
						break;
					case "3":
						dataRegisters[registerLocationX] ^= dataRegisters[registerLocationY];
						programCounter += 2;
						break;
					case "4":
						int sum = dataRegisters[registerLocationX] + dataRegisters[registerLocationY];
						dataRegisters[0xF] = (sum > FIT_8BIT_REGISTER) ? 1 : 0;
						dataRegisters[registerLocationX] = sum & FIT_8BIT_REGISTER;
						programCounter += 2;
						break;
					case "5":
						dataRegisters[0xF] = (dataRegisters[registerLocationX] > dataRegisters[registerLocationY]) ? 1 : 0;
						dataRegisters[registerLocationX] = convertToUnsignedInt(dataRegisters[registerLocationX] - dataRegisters[registerLocationY]) & FIT_8BIT_REGISTER;
						programCounter += 2;
						break;
					case "6":
						dataRegisters[0xF] = dataRegisters[registerLocationX] & 0x1;
						dataRegisters[registerLocationX] = (dataRegisters[registerLocationX] >> 1) & FIT_8BIT_REGISTER;
						programCounter += 2;
						break;
					case "7":
						dataRegisters[0xF] = (dataRegisters[registerLocationX] > dataRegisters[registerLocationY]) ? 0 : 1;
						dataRegisters[registerLocationX] = convertToUnsignedInt(dataRegisters[registerLocationY] - dataRegisters[registerLocationX]) & FIT_8BIT_REGISTER;
						programCounter += 2;
						break;
					case "E":
						dataRegisters[0xF] = (dataRegisters[registerLocationX] >> 7) & 0x1;
						dataRegisters[registerLocationX] = (dataRegisters[registerLocationX] << 1) & FIT_8BIT_REGISTER;
						programCounter += 2;
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & FIT_16BIT_REGISTER) + " at (8) location " + programCounter);
						break;
				}
				break;
			case "9000":
				programCounter += (dataRegisters[registerLocationX] != dataRegisters[registerLocationY]) ? 4 : 2;
				break;
			case "A000":
				indexRegister = address;
				programCounter += 2;
				break;
			case "B000":
				programCounter = dataRegisters[0] + address;
				break;
			case "C000":
				randomNumber = new Random().nextInt(FIT_8BIT_REGISTER);
				dataRegisters[registerLocationX] = randomNumber & lowestByte;
				programCounter += 2;
				break;
			case "D000":
				dataRegisters[0xF] = 0x0;
				for (int row = 0; row < nibble; row++) {
					int memoryByte = getMemory().read(indexRegister + row);
					int coordinateY = dataRegisters[registerLocationY] + row;
					for (int column = 0; column < 8; column++) {
						if ((memoryByte & (0x80 >> column)) != 0) {
							int coordinateX = dataRegisters[registerLocationX] + column;
							int data = displayBuffer.read(convertToIndex(coordinateX, coordinateY));
							if (data != 0) {
								dataRegisters[0xF] = 0x1;
							} 
							displayBuffer.write(data^1, convertToIndex(coordinateX, coordinateY));
							dirtyBuffer.write(data^1, convertToIndex(coordinateX, coordinateY));
						}
					}
				}
				drawFlag = true;
				programCounter += 2;
				break;
			case "E000":
				switch(Integer.toHexString(lowestByte).toUpperCase()) {
					case "9E":
						programCounter += getKeyboard().getCurrentlyPressedKey() == dataRegisters[registerLocationX] ? 4 : 2;
						break;
					case "A1":
						programCounter += getKeyboard().getCurrentlyPressedKey() != dataRegisters[registerLocationX] ? 4 : 2;
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & FIT_16BIT_REGISTER) + " at (E) location " + programCounter);
						break;
				}
				break;
			case "F000":
				switch(Integer.toHexString(lowestByte).toUpperCase()) {
					case "7":
						dataRegisters[registerLocationX] = delayTimer;
						programCounter += 2;
						break;
					case "A":
						while (getKeyboard().getCurrentlyPressedKey() == 0) {
							;
						}
						dataRegisters[registerLocationX] = getKeyboard().getCurrentlyPressedKey();
						programCounter += 2;
						break;
					case "15":
						delayTimer = dataRegisters[registerLocationX];
						programCounter += 2;
						break;
					case "18":
						soundTimer = dataRegisters[registerLocationX] == 1 ? 2 : dataRegisters[registerLocationX];
						programCounter += 2;
						break;
					case "1E":
						int sum = (indexRegister + dataRegisters[registerLocationX]) & FIT_16BIT_REGISTER;
						dataRegisters[0xF] = sum > 0xFFF ? 1 : 0;
						indexRegister = sum;
						programCounter += 2;
						break;
					case "29":
				 		indexRegister = (dataRegisters[registerLocationX] * 5) & FIT_16BIT_REGISTER;
				 		programCounter += 2;
						break;
					case "33":
				 		getMemory().write(dataRegisters[registerLocationX] / 100, indexRegister);
				 		getMemory().write((dataRegisters[registerLocationX] % 100) / 10, indexRegister + 1);
				 		getMemory().write(dataRegisters[registerLocationX] % 10, indexRegister + 2);
				 		programCounter += 2;
						break;
					case "55":
						for (int i = 0; i <= registerLocationX; i++) {
							getMemory().write(dataRegisters[i], indexRegister + i);
						}
						programCounter += 2;
						break;
					case "65":
						for (int i = 0; i <= registerLocationX; i++) {
							dataRegisters[i] = getMemory().read(indexRegister + i);
						}
						programCounter += 2;
						break;
					default:
						System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & FIT_16BIT_REGISTER) + " at (f) location " + programCounter);
						break;
				}
				break;
			default:
				System.out.println("Unknown instruction " + Integer.toHexString(instructionRegister & FIT_16BIT_REGISTER) + " at (all) location " + programCounter);
				break;
		}
		programCounter &= FIT_16BIT_REGISTER;
	}
	
	private int convertToUnsignedInt(int value) {
		return value < 0 ? value + 65536 : value;
	}
	
	private int convertToIndex(int coordinateX, int coordinateY) {
		return (coordinateX % getScreen().width()) + ((coordinateY % getScreen().width()) * getScreen().width());
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
}
