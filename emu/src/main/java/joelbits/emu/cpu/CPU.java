package joelbits.emu.cpu;

import java.util.List;
import java.util.Stack;

import joelbits.emu.cpu.registers.Register;
import joelbits.emu.flags.Flag;
import joelbits.emu.input.Keyboard;
import joelbits.emu.memory.BufferFactory;
import joelbits.emu.memory.Memory;
import joelbits.emu.output.Screen;
import joelbits.emu.output.Sound;
import joelbits.emu.timers.Timer;

/**
 * There are 16 data registers named from V0 to VF. The carry flag (VF) is set to 1 if any screen pixels are flipped from set 
 * to unset when a sprite is drawn and set to 0 otherwise. Last 8 bits of each register are used to represent an unsigned byte.
 * In this CPU implementation components like the control unit (CU) and the arithmetic-logic unit (ALU) are abstracted away.
 *
 */
public class CPU {
	private final MemoryBus memoryBus = new MemoryBus();
	private final ExpansionBus expansionBus = new ExpansionBus();
	private final Memory displayBuffer = BufferFactory.createDisplayBuffer(getScreen().width(), getScreen().height());
	private final Memory dirtyBuffer = BufferFactory.createDirtyBuffer();
	private final Stack<Integer> stack = new Stack<Integer>();
	private final List<Register<Integer>> dataRegisters;
	private final Register<Integer> instructionRegister;
	private final Register<Integer> programCounter;
	private final Register<Integer> indexRegister;
	private final ALU alu;
	private Timer<Integer> delayTimer;
	private Timer<Integer> soundTimer;
	private Flag drawFlag;
	private Flag clearFlag;
	
	private final int FIT_8BIT_REGISTER = 0xFF;
	private final int FIT_16BIT_REGISTER = 0xFFFF;
	private int registerLocationX;
	private int registerLocationY;
	private int nibble;
	private int address;
	private int lowestByte;
	
	public CPU(List<Register<Integer>> dataRegisters, Register<Integer> instructionRegister, Register<Integer> programCounter, Register<Integer> indexRegister, List<Timer<Integer>> timers, List<Flag> flags, ALU alu) {
		this.dataRegisters = dataRegisters;
		this.instructionRegister = instructionRegister;
		this.programCounter = programCounter;
		this.indexRegister = indexRegister;
		this.alu = alu;
		assignTimers(timers);
		assignFlags(flags);
	}
	
	private void assignTimers(List<Timer<Integer>> timers) {
		String className;
		for (Timer<Integer> timer : timers) {
			className = timer.getClass().getName();
			if (className.equals("joelbits.emu.timers.SoundTimer")) {
				soundTimer = timer;
			} else if (className.equals("joelbits.emu.timers.DelayTimer")) {
				delayTimer = timer;
			}
		}
	}
	
	private void assignFlags(List<Flag> flags) {
		String className;
		for (Flag flag : flags) {
			className = flag.getClass().getName();
			if (className.equals("joelbits.emu.flags.ClearFlag")) {
				clearFlag = flag;
			} else if (className.equals("joelbits.emu.flags.DrawFlag")) {
				drawFlag = flag;
			}
		}
	}
	
	public Keyboard getKeyboard() {
		return expansionBus.getKeyboard();
	}
	
	public Memory getDisplayBuffer() {
		return displayBuffer;
	}
	
	public Memory getDirtyBuffer() {
		return dirtyBuffer;
	}
	
	public Sound getSound() {
		return expansionBus.getSound();
	}
	
	public Screen<Integer> getScreen() {
		return expansionBus.getScreen();
	}
	
	public Memory getPrimaryMemory() {
		return memoryBus.getPrimaryMemory();
	}
	
	public void initialize(int address, int instruction, int index, int delayTime, int soundTime, int[] data) {
		programCounter.write(address);
		delayTimer.setValue(delayTime);
		soundTimer.setValue(soundTime);
		indexRegister.write(index);
		instructionRegister.write(instruction);
		
		getDirtyBuffer().clear();
		getDisplayBuffer().clear();
		getPrimaryMemory().clear();
		
		for (int i = 0; i < data.length; i++) {
			getPrimaryMemory().write(i, data[i]);
		}
	}
	
	public void resetDataRegisters() {
		for (int i = 0; i < dataRegisters.size(); i++) {
			dataRegisters.get(i).write(0);
		}
	}
	
	public void loadROM(byte[] ROM, int startLocation) {
		for (int i = 0, location = startLocation; i < ROM.length; i++, location++) {
			getPrimaryMemory().write(location, Byte.toUnsignedInt(ROM[i]));
		}
	}
	
	public void executeOperation() {
		int instruction = getPrimaryMemory().read(programCounter.read()) << 8 | getPrimaryMemory().read(programCounter.read()+1);
		instructionRegister.write(Integer.valueOf(instruction));
		
		registerLocationX = (instruction & 0x0F00) >> 8;
		registerLocationY = (instruction & 0x00F0) >> 4;
		nibble = instruction & 0x000F;
		address = instruction & 0x0FFF;
		lowestByte = instruction & 0x00FF;
		String leastSignificantNibble = Integer.toHexString(nibble).toUpperCase();
		String leastSignificantByte = Integer.toHexString(lowestByte).toUpperCase();
		
		switch(Integer.toHexString(instruction & 0xF000).toUpperCase()) {
			case "0":
				if (Integer.toHexString(lowestByte).toUpperCase().equals("E0")) {
					clearDisplayBuffers();
					programCounter.write(programCounter.read() + 2);
				} else if (Integer.toHexString(lowestByte).toUpperCase().equals("EE")) {
					programCounter.write(stack.pop() + 2);
				}
				break;
			case "1000":
				programCounter.write(address);
				break;
			case "2000":
				stack.push(programCounter.read());
				programCounter.write(address);
				break;
			case "3000":
				alu.skipNextIfEqual(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case "4000":
				alu.skipNextIfNotEqual(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case "5000":
				alu.skipNextIfEqual(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				break;
			case "6000":
				alu.load(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case "7000":
				alu.add(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case "8000":
				if (leastSignificantNibble.equals("0")) {
					alu.load(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				} else if (leastSignificantNibble.equals("1")) {
					alu.bitwiseOR(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				} else if (leastSignificantNibble.equals("2")) {
					alu.bitwiseAND(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				} else if (leastSignificantNibble.equals("3")) {
					alu.bitwiseXOR(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				} else if (leastSignificantNibble.equals("4")) {
					alu.addWithCarry(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read(), FIT_8BIT_REGISTER);
				} else if (leastSignificantNibble.equals("5")) {
					alu.subtractWithBorrow(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				} else if (leastSignificantNibble.equals("6")) {
					alu.rightShiftWithCarry(dataRegisters.get(registerLocationX));
				} else if (leastSignificantNibble.equals("7")) {
					alu.subtractWithNegatedBorrow(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				} else if (leastSignificantNibble.equals("E")) {
					alu.leftShiftWithCarry(dataRegisters.get(registerLocationX));
				}
				break;
			case "9000":
				alu.skipNextIfNotEqual(dataRegisters.get(registerLocationX), dataRegisters.get(registerLocationY).read());
				break;
			case "A000":
				alu.load(indexRegister, address);
				break;
			case "B000":
				programCounter.write(dataRegisters.get(0x0).read() + address);
				break;
			case "C000":
				alu.addWithRandom(dataRegisters.get(registerLocationX), lowestByte);
				break;
			case "D000":
				dataRegisters.get(0xF).write(0);
				for (int row = 0; row < nibble; row++) {
					int memoryByte = getPrimaryMemory().read(indexRegister.read() + row);
					int coordinateY = dataRegisters.get(registerLocationY).read() + row;
					for (int column = 0; column < 8; column++) {
						if ((memoryByte & (0x80 >> column)) != 0) {
							int coordinateX = dataRegisters.get(registerLocationX).read() + column;
							int data = getDisplayBuffer().read(convertToIndex(coordinateX, coordinateY));
							if (data != 0) {
								dataRegisters.get(0xF).write(1);
							} 
							getDisplayBuffer().write(convertToIndex(coordinateX, coordinateY), data^1);
							getDirtyBuffer().write(convertToIndex(coordinateX, coordinateY), data^1);
						}
					}
				}
				if (!drawFlag.isActive()) {
					drawFlag.toggle();
				}
				programCounter.write(programCounter.read() + 2);
				break;
			case "E000":
				if (leastSignificantByte.equals("9E")) {
					alu.skipNextIfEqual(dataRegisters.get(registerLocationX), getKeyboard().getCurrentlyPressedKey());
				} else if (leastSignificantByte.equals("A1")) {
					alu.skipNextIfNotEqual(dataRegisters.get(registerLocationX), getKeyboard().getCurrentlyPressedKey());
				}
				break;
			case "F000":
				if (leastSignificantNibble.equals("7")) {
					alu.load(dataRegisters.get(registerLocationX), delayTimer.currentValue());
				} else if (leastSignificantNibble.equals("A")) {
					while (getKeyboard().getCurrentlyPressedKey() == 0) {
						;
					}
					alu.load(dataRegisters.get(registerLocationX), getKeyboard().getCurrentlyPressedKey());
				} else if (leastSignificantByte.equals("15")) {
					delayTimer.setValue(dataRegisters.get(registerLocationX).read());
					programCounter.write(programCounter.read() + 2);
				} else if (leastSignificantByte.equals("18")) {
					int value = dataRegisters.get(registerLocationX).read();
					soundTimer.setValue(value == 1 ? 2 : value);
					programCounter.write(programCounter.read() + 2);
				} else if (leastSignificantByte.equals("1E")) {
					alu.addWithCarry(indexRegister, dataRegisters.get(registerLocationX).read(), 0xFFF);
				} else if (leastSignificantByte.equals("29")) {
					alu.load(indexRegister, (dataRegisters.get(registerLocationX).read() * 5) & FIT_16BIT_REGISTER);
				} else if (leastSignificantByte.equals("33")) {
					getPrimaryMemory().write(indexRegister.read(), dataRegisters.get(registerLocationX).read() / 100);
			 		getPrimaryMemory().write(indexRegister.read() + 1, (dataRegisters.get(registerLocationX).read() % 100) / 10);
			 		getPrimaryMemory().write(indexRegister.read() + 2, dataRegisters.get(registerLocationX).read() % 10);
			 		programCounter.write(programCounter.read() + 2);
				} else if (leastSignificantByte.equals("55")) {
					for (int i = 0; i <= registerLocationX; i++) {
						getPrimaryMemory().write(indexRegister.read() + i, dataRegisters.get(i).read());
					}
					programCounter.write(programCounter.read() + 2);
				} else if (leastSignificantByte.equals("65")) {
					for (int i = 0; i <= registerLocationX; i++) {
						dataRegisters.get(i).write(getPrimaryMemory().read(indexRegister.read() + i));
					}
					programCounter.write(programCounter.read() + 2);
				}
				break;
			default:
				System.out.println("Unknown instruction " + Integer.toHexString(instruction & FIT_16BIT_REGISTER) + " at (all) location " + programCounter.read());
				break;
		}
		programCounter.write(programCounter.read() & FIT_16BIT_REGISTER);
	}
	
	private void clearDisplayBuffers() {
		getDisplayBuffer().clear();
		getDirtyBuffer().clear();
		if (!clearFlag.isActive()) {
			clearFlag.toggle();
		}
	}
	
	private int convertToIndex(int coordinateX, int coordinateY) {
		int screenWidth = getScreen().width();
		return (coordinateX % screenWidth) + ((coordinateY % screenWidth) * screenWidth);
	}
	
	public int readStackTopValue() {
		return stack.isEmpty() ? -1 : stack.peek();
	}
}
