package joelbits.emu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import javafx.scene.input.KeyCode;
import joelbits.emu.cpu.CPU;
import joelbits.emu.memory.Memory;
import joelbits.emu.output.Display;

public class TestCPU {
	private CPU target;
	private Memory memory;
	private int[] dataRegisters = {43, 176, 40, 206, 33, 148, 33, 136, 77, 29, 48, 81, 30, 8, 34, 0};
	private int[] fontset = new int[80];
	private int programCounter = 0x200;
	private int instructionRegister = 0x0;
	private int indexRegister = 0x250;
	private int delayTimer = 0x0;
	private int soundTimer = 0x0;
	private final int FIT_8BIT_REGISTER = 0xFF;
	private final int FIT_16BIT_REGISTER = 0xFFFF;
	
	@Before
	public void setUp() {
		target = new CPU();
		target.initialize(programCounter, instructionRegister, indexRegister, delayTimer, soundTimer, dataRegisters, fontset);
		memory = target.getMemory();
	}
	
	/**
	 * 00E0 - CLS
	 * 
	 * Clear the display.
	 */
	@Test
	public void setDisplayBufferValuesToZero() {
		target.getDisplay().writeToDisplayBuffer(0x59, 0x345);
		target.getDisplay().writeToDisplayBuffer(0x53, 0x298);
		executeOpCode(0x00E0);
		
		for (int i = 0; i < Display.SCREEN_HEIGHT * Display.SCREEN_WIDTH; i++) {
			assertEquals(0x0, target.getDisplay().readFromDisplayBuffer(i));
		}
		assertTrue(target.isClearFlag());
	}
	
	/**
	 * 00EE - RET
	 * 
	 * Return from a subroutine. The interpreter sets the program counter to the address at the top of the stack, then subtracts 1 from 
	 * the stack pointer.
	 */
	@Test
	public void popTopOfStackAddressIntoProgramCounter() {
		executeOpCode(0x2567);
		assertEquals(programCounter+2, target.readStackTopValue());
		
		writeToMemory(0x0, 0x567);
		writeToMemory(0xEE, 0x568);
		executeOpCode(0x00EE);
		
		assertEquals(-1, target.readStackTopValue());
		assertEquals(programCounter+2 , target.readProgramCounter());
	}
	
	/**
	 * 1nnn - JP addr
	 * 
	 * Jump to location nnn. The interpreter sets the program counter to nnn.
	 */
	@Test
	public void storeAddressInProgramCounter() {
		executeOpCode(0x1AB5);
		
		assertEquals(0xAB5, target.readProgramCounter());
	}
	
	/**
	 * 2nnn - CALL addr
	 * 
	 * Call subroutine at nnn. The interpreter increments the stack pointer, then puts the current program counter value on the top of the stack. 
	 * The PC is then set to nnn.
	 */
	@Test
	public void pushProgramCounterValueOntoStack() {
		executeOpCode(0x2567);
		
		assertEquals(programCounter+2, target.readStackTopValue());
		assertEquals(0x567, target.readProgramCounter());
	}
	
	/**
	 * 3xkk - SE Vx, byte
	 * 
	 * Skip next instruction if Vx = kk. The interpreter compares register Vx to kk, and if they are equal, increments the program counter by 2.
	 */
	@Test
	public void skipNextInstructionSinceDataRegisterValueAndLowestByteEqual() {
		executeOpCode(0x3421);

		assertEquals(programCounter + 4, target.readProgramCounter());
	}
	
	/**
	 * 3xkk - SE Vx, byte
	 * 
	 * Skip next instruction if Vx = kk. The interpreter compares register Vx to kk, and if they are equal, increments the program counter by 2.
	 */
	@Test
	public void doNotSkipNextInstructionSinceDataRegisterValueAndLowestByteNotEqual() {
		executeOpCode(0x339E);
		
		assertEquals(programCounter + 2, target.readProgramCounter());
	}
	
	/**
	 * 4xkk - SNE Vx, byte
	 * 
	 * Skip next instruction if Vx != kk. The interpreter compares register Vx to kk, and if they are not equal, increments the 
	 * program counter by 2.
	 */
	@Test
	public void skipNextInstructionSinceDataRegisterValueAndLowestByteNotEqual() {
		executeOpCode(0x439E);
		
		assertEquals(programCounter + 4, target.readProgramCounter());
	}
	
	/**
	 * 4xkk - SNE Vx, byte
	 * 
	 * Skip next instruction if Vx != kk. The interpreter compares register Vx to kk, and if they are not equal, increments the 
	 * program counter by 2.
	 */
	@Test
	public void doNotSkipNextInstructionSinceDataRegisterValueAndLowestByteAreEqual() {
		executeOpCode(0x4421);

		assertEquals(programCounter + 2, target.readProgramCounter());
	}
	
	/**
	 * 5xy0 - SE Vx, Vy
	 * 
	 * Skip next instruction if Vx = Vy. The interpreter compares register Vx to register Vy, and if they are equal, increments the program
	 * counter by 2.
	 */
	@Test
	public void skipNextInstructionSinceDataRegisterValuesEqual() {
		executeOpCode(0x5460);
		
		assertEquals(programCounter + 4, target.readProgramCounter());
	}
	
	/**
	 * 5xy0 - SE Vx, Vy
	 * 
	 * Skip next instruction if Vx = Vy. The interpreter compares register Vx to register Vy, and if they are equal, increments the program
	 * counter by 2.
	 */
	@Test
	public void doNotSkipNextInstructionSinceDataRegisterValuesNotEqual() {
		executeOpCode(0x5180);
		
		assertEquals(programCounter + 2, target.readProgramCounter());
	}
	
	/**
	 * 6xkk - LD Vx, byte
	 * 
	 * Set Vx = kk. The interpreter puts the value kk into register Vx.
	 */
	@Test
	public void storeLowestByteIntoDataRegister() {
		executeOpCode(0x63DA);
		
		assertEquals(0xDA, target.readDataRegister(0x3));
	}
	
	/**
	 * 7xkk - ADD Vx, byte
	 * 
	 * Set Vx = Vx + kk. Adds the value kk to the value of register Vx, then stores the result in Vx. 
	 */
	@Test
	public void addsLowestByteToDataRegister() {
		executeOpCode(0x7398);
		
		assertEquals((dataRegisters[0x3] + 0x98) & FIT_8BIT_REGISTER, target.readDataRegister(0x3));
	}
	
	/**
	 * 8xy0 - LD Vx, Vy
	 * 
	 * Set Vx = Vy. Stores the value of register Vy in register Vx.
	 */
	@Test
	public void storeDataRegisterValueInAnotherDataRegister() {
		executeOpCode(0x8DC0);
		
		assertEquals(dataRegisters[0xC], target.readDataRegister(0xD));
	}
	
	/**
	 * 8xy1 - OR Vx, Vy
	 * 
	 * Set Vx = Vx OR Vy. Performs a bitwise OR on the values of Vx and Vy, then stores the result in Vx.
	 */
	@Test
	public void storeBitwiseORedDataRegisterValuesInDataRegister() {
		int result = dataRegisters[0x3] | dataRegisters[0x4];
		executeOpCode(0x8341);
		
		assertEquals(result, target.readDataRegister(0x3));
	}
	
	/**
	 * 8xy2 - AND Vx, Vy
	 * 
	 * Set Vx = Vx AND Vy. Performs a bitwise AND on the values of Vx and Vy, then stores the result in Vx.
	 */
	@Test
	public void storeBitwiseANDedDataRegisterValuesInDataRegister() {
		int result = dataRegisters[0x3] & dataRegisters[0x4];
		executeOpCode(0x8342);
		
		assertEquals(result, target.readDataRegister(0x3));
	}
	
	/**
	 * 8xy3 - XOR Vx, Vy
	 * 
	 * Set Vx = Vx XOR Vy. Performs a bitwise exclusive OR on the values of Vx and Vy, then stores the result in Vx.
	 */
	@Test
	public void storeBitwiseXORedDataRegisterValuesInDataRegister() {
		int result = dataRegisters[0xD] ^ dataRegisters[0x5];
		executeOpCode(0x85D3);
		
		assertEquals(result, target.readDataRegister(0x5));
	}
	
	/**
	 * 8xy4 - ADD Vx, Vy
	 * 
	 * Set Vx = Vx + Vy, set VF = carry. The values of Vx and Vy are added together. If the result is greater than 8 bits (i.e., > 255,) VF is
	 * set to 1, otherwise 0. Only the lowest 8 bits of the result are kept, and stored in Vx.
	 */
	@Test
	public void sumDataRegisterValuesAndSetCarryBecauseSumGreaterThanEightBits() {
		executeOpCode(0x8134);
		
		assertEquals(1, target.readDataRegister(0xF));
		assertEquals(((dataRegisters[0x1] + dataRegisters[0x3]) & 0x34) & FIT_8BIT_REGISTER, target.readDataRegister(0x1));
	}
	
	/**
	 * 8xy4 - ADD Vx, Vy
	 * 
	 * Set Vx = Vx + Vy, set VF = carry. The values of Vx and Vy are added together. If the result is greater than 8 bits (i.e., > 255,) VF is
	 * set to 1, otherwise 0. Only the lowest 8 bits of the result are kept, and stored in Vx.
	 */
	@Test
	public void sumDataRegisterValuesAndDoNotSetCarryBecauseSumLessThanEightBits() {
		executeOpCode(0x8424);
		
		assertEquals(0, target.readDataRegister(0xF));
		assertEquals(((dataRegisters[0x4] + dataRegisters[0x2]) & 0x24) & FIT_8BIT_REGISTER, target.readDataRegister(0x4));
	}
	
	/**
	 * 8xy5 - SUB Vx, Vy
	 * 
	 * Set Vx = Vx - Vy, set VF = NOT borrow. If Vx > Vy, then VF is set to 1, otherwise 0. Then Vy is subtracted from Vx, and
	 * the results stored in Vx.
	 */
	@Test
	public void doNotSetBorrowSinceFirstDataRegisterLessThanSecondDataRegister() {
		executeOpCode(0x8235);
		
		assertEquals(0, target.readDataRegister(0xF));
		assertEquals((convertToUnsignedInt(dataRegisters[0x2] - dataRegisters[0x3]) & FIT_8BIT_REGISTER), target.readDataRegister(0x2));
	}
	
	/**
	 * 8xy5 - SUB Vx, Vy
	 * 
	 * Set Vx = Vx - Vy, set VF = NOT borrow. If Vx > Vy, then VF is set to 1, otherwise 0. Then Vy is subtracted from Vx, and
	 * the results stored in Vx.
	 */
	@Test
	public void setBorrowSinceFirstDataRegisterLargerThanSecondDataRegister() {
		executeOpCode(0x8325);
		
		assertEquals(1, target.readDataRegister(0xF));
		assertEquals((dataRegisters[0x3] - dataRegisters[0x2]) & FIT_8BIT_REGISTER, target.readDataRegister(0x3));
	}
	
	/**
	 * 8xy6 - SHR Vx {, Vy}
	 * 
	 * Set Vx = Vx SHR 1. If the least-significant bit of Vx is 1, then VF is set to 1, otherwise 0. Then Vx is divided by 2.
	 */
	@Test
	public void setCarryAndShiftRight() {
		executeOpCode(0x8456);
		
		assertEquals(1, target.readDataRegister(0xF));
		assertEquals(dataRegisters[0x4] >> 1, target.readDataRegister(0x4));
	}
	
	/**
	 * 8xy6 - SHR Vx {, Vy}
	 * 
	 * Set Vx = Vx SHR 1. If the least-significant bit of Vx is 1, then VF is set to 1, otherwise 0. Then Vx is divided by 2.
	 */
	@Test
	public void doNotSetCarryAndShiftRight() {
		executeOpCode(0x8AB6);
		
		assertEquals(0, target.readDataRegister(0xF));
		assertEquals(dataRegisters[0xA] >> 1, target.readDataRegister(0xA));
	}
	
	/**
	 * 8xy7 - SUBN Vx, Vy
	 * 
	 * Set Vx = Vy - Vx, set VF = NOT borrow. If Vy > Vx, then VF is set to 1, otherwise 0. Then Vx is subtracted from Vy, and 
	 * the results stored in Vx.
	 */
	@Test
	public void setBorrowAndSubtractFirstDataRegisterValueFromSecondDataRegister() {
		executeOpCode(0x8017);
		
		assertEquals(1, target.readDataRegister(0xF));
		assertEquals(convertToUnsignedInt(dataRegisters[0x1] - dataRegisters[0x0]) & FIT_8BIT_REGISTER, target.readDataRegister(0x0));
	}
	
	/**
	 * 8xy7 - SUBN Vx, Vy
	 * 
	 * Set Vx = Vy - Vx, set VF = NOT borrow. If Vy > Vx, then VF is set to 1, otherwise 0. Then Vx is subtracted from Vy, and 
	 * the results stored in Vx.
	 */
	@Test
	public void doNotSetBorrowAndSubtractFirstDataRegisterValueFromSecondDataRegister() {
		executeOpCode(0x8107);
		
		assertEquals(0, target.readDataRegister(0xF));
		assertEquals(convertToUnsignedInt(dataRegisters[0x0] - dataRegisters[0x1]) & FIT_8BIT_REGISTER, target.readDataRegister(0x1));
	}
	
	/**
	 * 8xyE - SHL Vx {, Vy}
	 * 
	 * Set Vx = Vx SHL 1. If the most-significant bit of Vx is 1, then VF is set to 1, otherwise to 0. Then Vx is multiplied by 2.
	 */
	@Test
	public void setDataRegisterToZeroSinceMostSignificantBitIsNotOne() {
		executeOpCode(0x8ABE);
		
		assertEquals(0, target.readDataRegister(0xF));
		assertEquals((dataRegisters[0xA] << 1) & FIT_8BIT_REGISTER, target.readDataRegister(0xA));
	}
	
	/**
	 * 8xyE - SHL Vx {, Vy}
	 * 
	 * Set Vx = Vx SHL 1. If the most-significant bit of Vx is 1, then VF is set to 1, otherwise to 0. Then Vx is multiplied by 2.
	 */
	@Test
	public void setDataRegisterToOneSinceMostSignificantBitIsOne() {
		executeOpCode(0x835E);
		
		assertEquals(1, target.readDataRegister(0xF));
		assertEquals((dataRegisters[0x3] << 1) & FIT_8BIT_REGISTER, target.readDataRegister(0x3));
	}
	
	/**
	 * 9xy0 - SNE Vx, Vy
	 * 
	 * Skip next instruction if Vx != Vy. The values of Vx and Vy are compared, and if they are not equal, the program counter is increased by 2.
	 */
	@Test
	public void skipNextInstructionSinceDataRegisterValuesNotEqual() {
		executeOpCode(0x95C0);
		
		assertEquals(programCounter + 4, target.readProgramCounter());
	}
	
	/**
	 * 9xy0 - SNE Vx, Vy
	 * 
	 * Skip next instruction if Vx != Vy. The values of Vx and Vy are compared, and if they are not equal, the program counter is increased by 2.
	 */
	@Test
	public void doNotSkipNextInstructionSinceDataRegisterValuesAreEqual() {
		executeOpCode(0x9460);
		
		assertEquals(programCounter + 2, target.readProgramCounter());
	}
	
	/**
	 * Annn - LD I, addr
	 * 
	 * Set I = nnn. The value of the index register is set to nnn.
	 */
	@Test
	public void storeAddressInIndexRegister() {
		executeOpCode(0xAEBA);
		
		assertEquals(0xEBA, target.readIndexRegister());
	}
	
	/**
	 * Bnnn - JP V0, addr
	 * 
	 * Jump to location nnn + V0. The program counter is set to nnn plus the value of V0.
	 */
	@Test
	public void setProgramCounterToAddressPlusDataRegisterValue() {
		executeOpCode(0xB348);

		assertEquals(0x348 + dataRegisters[0], target.readProgramCounter());
	}
	
	/**
	 * Cxkk - RND Vx, byte
	 * 
	 * Set Vx = random byte AND kk. The interpreter generates a random number from 0 to 255, which is then ANDed with the value kk. 
	 * The results are stored in Vx.
	 */
	@Test
	public void storeRandomizedValueInDataRegister() {
		executeOpCode(0xC023);
		
		int randomNumber = target.readRandomNumber();
		assertEquals(randomNumber & 0x23, target.readDataRegister(0x0));
	}
	
	/**
	 * Dxyn - DRW Vx, Vy, nibble
	 * 
	 * Display n-byte sprite starting at memory location I (index register) at (Vx, Vy), set VF = collision. The interpreter reads n bytes from 
	 * memory, starting at the address stored in the index register. These bytes are then displayed as sprites on screen at coordinates (Vx, Vy).
	 * Sprites are XORed onto the existing screen. If this causes any pixels to be erased, VF is set to 1, otherwise it is set to 0. If the 
	 * sprite is positioned so part of it is outside the coordinates of the display, it wraps around to the opposite side of the screen.
	 */
	@Test
	public void storeSpriteStartingAtIndexRegisterLocationIntoDataRegistersWithoutCollisions() {
		writeToMemory(0xF0, indexRegister);
		writeToMemory(0x10, indexRegister+1);
		writeToMemory(0xF0, indexRegister+2);
		writeToMemory(0x80, indexRegister+3);
		writeToMemory(0xF0, indexRegister+4);
		executeOpCode(0xD475);
		
		for (int row = 0; row < 0x5; row++) {
			int coordinateY = dataRegisters[0x7] + row;
			int memoryByte = target.getMemory().readFromMemory(indexRegister + row);
			for (int column = 0; column < 8; column++) {
				int coordinateX = dataRegisters[0x4] + column;
				if ((memoryByte & (0x80 >> column)) != 0) {
					assertTrue(target.getDisplay().readFromDisplayBuffer(coordinateX, coordinateY) != 0);
				} else {
					assertTrue(target.getDisplay().readFromDisplayBuffer(coordinateX, coordinateY) == 0);
				}
			}
		}
		assertEquals(0, target.readDataRegister(0xF));
		assertTrue(target.isDrawFlag());
	}
	
	/**
	 * Ex9E - SKP Vx
	 * 
	 * Skip next instruction if key with the value of Vx is pressed. Checks the keyboard, and if the key corresponding to the
	 * value of Vx is currently in the down position, the program counter is increased by 2.
	 */
	@Test
	public void doNotSkipNextInstructionBecauseKeyEqualToDataRegisterValueIsNotPressed() {
		target.getKeyboard().keyPressed(KeyCode.R);
		executeOpCode(0xEA9E);
		
		assertFalse(target.getKeyboard().getCurrentlyPressedKey() == target.readDataRegister(0xA));
		assertEquals(programCounter + 2, target.readProgramCounter());
	}
	
	/**
	 * Ex9E - SKP Vx
	 * 
	 * Skip next instruction if key with the value of Vx is pressed. Checks the keyboard, and if the key corresponding to the
	 * value of Vx is currently in the down position, the program counter is increased by 2.
	 */
	@Test
	public void skipNextInstructionBecauseKeyEqualToDataRegisterValueIsPressed() {
		target.getKeyboard().keyPressed(KeyCode.R);
		executeOpCode(0xED9E);
		
		assertEquals(target.getKeyboard().getCurrentlyPressedKey(), target.readDataRegister(0xD));
		assertEquals(programCounter + 4, target.readProgramCounter());
	}
	
	/**
	 * ExA1 - SKNP Vx
	 * 
	 * Skip next instruction if key with the value of Vx is not pressed. Checks the keyboard, and if the key corresponding to 
	 * the value of Vx is currently in the up position, the program counter is increased by 2.
	 */
	@Test
	public void skipNextInstructionBecauseKeyEqualToDataRegisterValueIsNotPressed() {
		target.getKeyboard().keyPressed(KeyCode.R);
		executeOpCode(0xEAA1);
		
		assertFalse(target.getKeyboard().getCurrentlyPressedKey() == target.readDataRegister(0xA));
		assertEquals(programCounter + 4, target.readProgramCounter());
	}
	
	/**
	 * ExA1 - SKNP Vx
	 * 
	 * Skip next instruction if key with the value of Vx is not pressed. Checks the keyboard, and if the key corresponding to 
	 * the value of Vx is currently in the up position, the program counter is increased by 2.
	 */
	@Test
	public void doNotSkipNextInstructionBecauseKeyEqualToDataRegisterValueIsPressed() {
		target.getKeyboard().keyPressed(KeyCode.R);
		executeOpCode(0xEDA1);
		
		assertEquals(target.getKeyboard().getCurrentlyPressedKey(), target.readDataRegister(0xD));
		assertEquals(programCounter + 2, target.readProgramCounter());
	}
	
	/**
	 * Fx07 - LD Vx, DT
	 * 
	 * Set Vx = delay timer value. The value of the delay timer is placed into Vx.
	 */
	@Test
	public void storeDelayTimerValueInDataRegister() {
		executeOpCode(0xF207);
		
		assertEquals(target.readDelayTimer(), target.readDataRegister(0x2));
	}
	
	/**
	 * Fx0A - LD Vx, K
	 * 
	 * Wait for a key press, store the value of the key in Vx. All execution stops until a key is pressed, then the value of 
	 * that key is stored in Vx.
	 */
	@Test
	public void valueOfPressedKeyStoredInDataRegister() {
		target.getKeyboard().keyPressed(KeyCode.A);
		executeOpCode(0xF70A);
		
		assertEquals(target.getKeyboard().getCurrentlyPressedKey(), target.readDataRegister(0x7));
	}
	
	/**
	 * Fx15 - LD DT, Vx
	 * 
	 * Set delay timer = Vx. Delay timer is set equal to the value of Vx.
	 */
	@Test
	public void setDelayTimerEqualToDataRegisterValue() {
		executeOpCode(0xF615);
		
		assertEquals(dataRegisters[0x6], target.readDelayTimer());
	}
	
	/**
	 * Fx18 - LD ST, Vx
	 * 
	 * Set sound timer = Vx. Sound timer is set equal to the value of Vx.
	 */
	@Test
	public void setSoundTimerEqualToDataRegisterValue() {
		executeOpCode(0xF518);
		
		assertEquals(dataRegisters[0x5], target.readSoundTimer());
	}
	
	/**
	 * Fx1E - ADD I, Vx
	 * 
	 * Set I = I + Vx. The values of I and Vx are added, and the results are stored in I.
	 */
	@Test
	public void storeIndexRegisterPlusDataRegisterValueInIndexRegister() {
		executeOpCode(0xFD1E);
		
		assertEquals((indexRegister + dataRegisters[0xD]) & FIT_16BIT_REGISTER, target.readIndexRegister());
	}
	
	/**
	 * Fx29 - LD F, Vx
	 * 
	 * Set I = location of sprite for digit Vx. The value of I is set to the location for the hexadecimal sprite corresponding 
	 * to the value of Vx.
	 */
	@Test
	public void storeSpriteLocationInIndexRegister() {
		executeOpCode(0xFD29);
		
		assertEquals(dataRegisters[0xD]*5 & FIT_16BIT_REGISTER, target.readIndexRegister());
	}
	
	/**
	 * Fx33 - LD B, Vx
	 * 
	 * Store BCD representation of Vx in memory locations I, I+1, and I+2. The interpreter takes the decimal 
	 * value of Vx, and places the hundreds digit in memory at location in I, the tens digit at location I+1, 
	 * and the ones digit at location I+2.
	 */
	@Test
	public void storeBinaryCodedDecimalRepresentationOfDataRegisterValue() {
		executeOpCode(0xF733);

		assertEquals(1, memory.readFromMemory(indexRegister));
		assertEquals(3, memory.readFromMemory(indexRegister+1));
		assertEquals(6, memory.readFromMemory(indexRegister+2));
	}
	
	/**
	 * Fx55 - LD [I], Vx
	 * 
	 * Store registers V0 through Vx in memory starting at location I. The interpreter copies the values of registers V0 through Vx into 
	 * memory, starting at the address in I.
	 */
	@Test
	public void storeDataRegisterValuesInMemoryStartingAtLocationCorrespondingToIndexRegister() {
		executeOpCode(0xF755);

		for (int i = 0; i < 8; i++) {
			assertEquals(target.readDataRegister(i), memory.readFromMemory(indexRegister+i));
		}
	}
	
	/**
	 * Fx65 - LD Vx, [I]
	 * 
	 * Read registers V0 through Vx from memory starting at location I. The interpreter reads values from memory starting at 
	 * location I into registers V0 through Vx.
	 */
	@Test
	public void storeMemoryValuesStartingAtIndexRegisterInDataRegisters() {
		executeOpCode(0xF465);
		
		for (int i = 0; i < 5; i++) {
			assertEquals(memory.readFromMemory(indexRegister+i), target.readDataRegister(i));
		}
	}
	
	private void executeOpCode(int opcode) {
		writeToMemory((opcode >> 8) & FIT_8BIT_REGISTER, programCounter);
		writeToMemory(opcode & FIT_8BIT_REGISTER, programCounter+1);
		
		target.nextInstructionCycle();
	}
	
	private void writeToMemory(int data, int location) {
		memory.writeToMemory(data, location);
	}
	
	private int convertToUnsignedInt(int value) {
		return value < 0 ? value + 65536 : value;
	}
}
