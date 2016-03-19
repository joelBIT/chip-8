package joelbits.emu.cpu;

import java.util.Random;
import java.util.Stack;

import joelbits.emu.memory.Memory;

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
	
	public void initialize(int programCounter, int instructionRegister, int indexRegister, int delayTimer, int soundTimer, int[] fontset) {
		this.programCounter = programCounter;
		this.instructionRegister = instructionRegister;
		this.indexRegister = indexRegister;
		this.delayTimer = delayTimer;
		this.soundTimer = soundTimer;
		expansionBus.getDisplay().clearDisplayBuffer();
		Memory memory = memoryBus.getPrimaryMemory();
		memory.clearMemory();
		for (int i = 0; i < 80; i++) {
			memory.writeToMemory(fontset[i], i);
		}
	}
	
	public void loadROM(byte[] ROM, int startLocation) {
		for (int i = 0, location = startLocation; i < ROM.length; i++, location++) {
			memoryBus.getPrimaryMemory().writeToMemory(Byte.toUnsignedInt(ROM[i]), location);
		}
	}

	public void nextInstructionCycle() {
		instructionRegister = memoryBus.getPrimaryMemory().readFromMemory(programCounter) << 8 | memoryBus.getPrimaryMemory().readFromMemory(programCounter+1);
		programCounter += 2;
		
		switch(Integer.toHexString(instructionRegister & 0xF000)) {
			case "0":
				if ((instructionRegister & 0x000F) == 0) {
					expansionBus.getDisplay().clearDisplayBuffer();
				} else {
					programCounter = stack.pop();
				}
				break;
			case "1000":
				programCounter = instructionRegister & 0x0FFF;
				break;
			case "2000":
				stack.push(programCounter);
				programCounter = instructionRegister & 0x0FFF;
				break;
			case "3000":
				programCounter += (registers[(instructionRegister & 0x0F00) >> 8] == (instructionRegister & 0x00FF)) ? 2 : 0;
				break;
			case "4000":
				programCounter += (registers[(instructionRegister & 0x0F00) >> 8] != (instructionRegister & 0x00FF)) ? 2 : 0;
				break;
			case "5000":
				programCounter += (registers[(instructionRegister & 0x0F00) >> 8] == registers[(instructionRegister & 0x00F0) >> 4]) ? 2 : 0;
				break;
			case "6000":
				registers[(instructionRegister & 0x0F00) >> 8] = instructionRegister & 0x00FF;
				break;
			case "7000":
				registers[(instructionRegister & 0x0F00) >> 8] += instructionRegister & 0x00FF;
				break;
			case "8000":
				switch(Integer.toHexString(instructionRegister & 0x000F)) {
					case "0":
						registers[(instructionRegister & 0x0F00) >> 8] = registers[(instructionRegister & 0x00F0) >> 4];
						break;
					case "1":
						registers[(instructionRegister & 0x0F00) >> 8] |= registers[(instructionRegister & 0x00F0) >> 4];
						break;
					case "2":
						registers[(instructionRegister & 0x0F00) >> 8] &= registers[(instructionRegister & 0x00F0) >> 4];
						break;
					case "3":
						registers[(instructionRegister & 0x0F00) >> 8] ^= registers[(instructionRegister & 0x00F0) >> 4];
						break;
					case "4":
						registers[(instructionRegister & 0x0F00) >> 8] += registers[(instructionRegister & 0x00F0) >> 4];
						registers[15] = (registers[(instructionRegister & 0x0F00) >> 8] > 255) ? 1 : 0;
						if (registers[15] == 1) {
							registers[(instructionRegister & 0x0F00) >> 8] &= 0xFF;
						}
						break;
					case "5":
						registers[15] = (registers[(instructionRegister & 0x0F00) >> 8] > registers[(instructionRegister & 0x00F0) >> 4]) ? 1 : 0;
						registers[(instructionRegister & 0x0F00) >> 8] -= registers[(instructionRegister & 0x00F0) >> 4];
						break;
					case "6":
						registers[15] = (registers[(instructionRegister & 0x0F00) >> 8] & 0x1) == 1 ? 1 : 0;
						registers[(instructionRegister & 0x0F00) >> 8] >>= 1;
						break;
					case "7":
						registers[15] = (registers[(instructionRegister & 0x0F00) >> 8] < registers[(instructionRegister & 0x00F0) >> 4]) ? 1 : 0;
						registers[(instructionRegister & 0x00F0) >> 4] -= registers[(instructionRegister & 0x0F00) >> 8];
						break;
					case "e":
						registers[15] = (registers[(instructionRegister & 0x0F00) >> 8] & 0x80) == 1 ? 1 : 0;
						registers[(instructionRegister & 0x0F00) >> 8] <<= 1;
						break;
					default:
						System.out.println("unknown opcode");
						break;
				}
				break;
			case "9000":
				programCounter += (registers[(instructionRegister & 0x0F00) >> 8] != registers[(instructionRegister & 0x00F0) >> 4]) ? 2 : 0;
				break;
			case "a000":
				indexRegister = instructionRegister & 0x0FFF;
				break;
			case "b000":
				programCounter = registers[0] + (instructionRegister & 0x0FFF);
				break;
			case "c000":
				registers[(instructionRegister & 0x0F00) >> 8] = (new Random().nextInt(256)) & (instructionRegister & 0x00FF);
				break;
			case "d000":
				registers[0xF] = 0;
				for (int row = 0; row < registers[(instructionRegister & 0x000F)]; row++) {
					int coordinateY = registers[(instructionRegister & 0x00F0) >> 4] + row;
					for (int column = 0; column < 8; column++) {
						int coordinateX = registers[(instructionRegister & 0x0F00) >> 8] + column;
						if ((memoryBus.getPrimaryMemory().readFromMemory((indexRegister + row)) & (0x80 >> column)) != 0) {
							registers[0xF] = expansionBus.getDisplay().readFromDisplayBuffer(coordinateX + (coordinateY)*64) == 1 ? 1 : registers[0xF];
							expansionBus.getDisplay().writeToDisplayBuffer((registers[0xF] ^= 1), (coordinateX + (coordinateY)*64));
						}
					}
					drawFlag = true;
				}
				break;
			case "e000":
				if ((instructionRegister & 0x000F) == 1) {
					programCounter += expansionBus.getKeyboard().getCurrentlyPressedKey() != expansionBus.getKeyboard().getKey(registers[(instructionRegister & 0x0F00) >> 8]) ? 2 : 0;
				} else {
					programCounter += expansionBus.getKeyboard().getCurrentlyPressedKey() == expansionBus.getKeyboard().getKey(registers[(instructionRegister & 0x0F00) >> 8]) ? 2 : 0;
				}
				break;
			case "f000":
				switch(Integer.toHexString(instructionRegister & 0x00FF)) {
					case "15":
						delayTimer = registers[(instructionRegister & 0x0F00) >> 8];
						break;
					case "18":
						soundTimer = registers[(instructionRegister & 0x0F00) >> 8];
						break;
					case "7":
						registers[(instructionRegister & 0x0F00) >> 8] = delayTimer;
						break;
					case "a":
						while (expansionBus.getKeyboard().getCurrentlyPressedKey() == 0) {
							;
						}
						registers[(instructionRegister & 0x0F00) >> 8] = expansionBus.getKeyboard().getCurrentlyPressedKey();
						break;
					case "1e":
						indexRegister += registers[(instructionRegister & 0x0F00) >> 8];
						break;
					case "29": 
				 		indexRegister = registers[(instructionRegister & 0x0F00) >> 8] * 5;
						break;
					case "33":
				 		memoryBus.getPrimaryMemory().writeToMemory(registers[(instructionRegister & 0x0F00) >> 8] / 100, indexRegister);
				 		memoryBus.getPrimaryMemory().writeToMemory((registers[(instructionRegister & 0x0F00) >> 8] / 10) % 10, indexRegister + 1);
				 		memoryBus.getPrimaryMemory().writeToMemory((registers[(instructionRegister & 0x0F00) >> 8] % 100) % 10, indexRegister + 2);
						break;
					case "55":
						for (int i = 0; i <= ((instructionRegister & 0x0F00) >> 8); i++, indexRegister++) {
							memoryBus.getPrimaryMemory().writeToMemory(registers[i], indexRegister);
						}
					case "65":
						for (int i = 0; i <= ((instructionRegister & 0x0F00) >> 8); i++, indexRegister++) {
							registers[i] = memoryBus.getPrimaryMemory().readFromMemory(indexRegister);
						}
					default:
						System.out.println("unknown opcode");
						break;
				}
				break;
			default:
				System.out.println("unknown opcode");
				break;
		}
	}
}
