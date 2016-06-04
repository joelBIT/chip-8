package joelbits.emu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import javafx.scene.input.KeyCode;
import joelbits.emu.cpu.CPU;
import joelbits.emu.cpu.ClearFlag;
import joelbits.emu.cpu.DelayTimer;
import joelbits.emu.cpu.DrawFlag;
import joelbits.emu.cpu.Flag;
import joelbits.emu.cpu.RandomNumberGenerator;
import joelbits.emu.cpu.SoundTimer;
import joelbits.emu.cpu.Timer;
import joelbits.emu.cpu.registers.DataRegister;
import joelbits.emu.cpu.registers.IndexRegister;
import joelbits.emu.cpu.registers.InstructionRegister;
import joelbits.emu.cpu.registers.ProgramCounter;
import joelbits.emu.cpu.registers.Register;
import joelbits.emu.memory.Memory;

public class TestCPU {
	private CPU target;
	private Memory memory;
	private List<Register<Integer>> dataRegisters;
	private Timer<Integer> delayTimer;
	private Timer<Integer> soundTimer;
	private Flag drawFlag;
	private Flag clearFlag;
	private InstructionRegister<Integer> instructionRegister;
	private ProgramCounter<Integer> programCounter;
	private IndexRegister<Integer> indexRegister;
	private RandomNumberGenerator randomNumberGenerator;
	
	private final int FIT_8BIT_REGISTER = 0xFF;
	private final int FIT_16BIT_REGISTER = 0xFFFF;
	private int[] dataRegisterValues = {43, 176, 40, 206, 33, 148, 33, 136, 77, 29, 48, 81, 30, 8, 1, 0};
	private int[] fontset = new int[80];
	private int address = 0x200;
	private int instruction = 0x0;
	private int index = 0x250;
	private int delayTime = 0x0;
	private int soundTime = 0x0;
	private int SCREEN_WIDTH = 64;
	private int SCREEN_HEIGHT = 32;
	
	@Before
	public void setUp() {
		dataRegisters = new ArrayList<>();
		for (int i = 0; i <= 0xF; i++) {
			dataRegisters.add(i, new DataRegister<Integer>());
			dataRegisters.get(i).write(dataRegisterValues[i]);
		}
		delayTimer = new DelayTimer<Integer>();
		soundTimer = new SoundTimer<Integer>();
		drawFlag = new DrawFlag();
		clearFlag = new ClearFlag();
		instructionRegister = new InstructionRegister<Integer>();
		programCounter = new ProgramCounter<Integer>();
		indexRegister = new IndexRegister<Integer>();
		randomNumberGenerator = new RandomNumberGenerator();
		
		target = new CPU(dataRegisters, instructionRegister, programCounter, indexRegister, Arrays.asList(delayTimer, soundTimer), Arrays.asList(drawFlag, clearFlag), randomNumberGenerator);
		target.initialize(address, instruction, index, delayTime, soundTime, fontset);
		
		memory = target.getPrimaryMemory();
	}
	
	/**
	 * 00E0 - CLS
	 * 
	 * Clear the display.
	 */
	@Test
	public void setDisplayBufferValuesToZero() {
		target.getDisplayBuffer().write(0x345, 0x59);
		target.getDisplayBuffer().write(0x298, 0x53);
		executeOpCode(0x00E0);
		
		for (int i = 0; i < SCREEN_HEIGHT * SCREEN_WIDTH; i++) {
			assertEquals(0x0, target.getDisplayBuffer().read(i));
		}
		assertTrue(clearFlag.isActive());
	}
	
	private void executeOpCode(int opcode) {
		writeToMemory(address, (opcode >> 8) & FIT_8BIT_REGISTER);
		writeToMemory(address + 1, opcode & FIT_8BIT_REGISTER);
		
		target.nextInstructionCycle();
	}
	
	private void writeToMemory(int location, int data) {
		memory.write(location, data);
	}
	
	private int convertToUnsignedInt(int value) {
		return value < 0 ? value + 65536 : value;
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
		assertEquals(address, target.readStackTopValue());
		
		writeToMemory(0x567, 0x0);
		writeToMemory(0x568, 0xEE);
		executeOpCode(0x00EE);
		
		assertEquals(-1, target.readStackTopValue());
		assertEquals(address+2 , programCounter.read().intValue());
	}
	
	/**
	 * 1nnn - JP addr
	 * 
	 * Jump to location nnn. The interpreter sets the program counter to nnn.
	 */
	@Test
	public void storeAddressInProgramCounter() {
		executeOpCode(0x1AB5);
		
		assertEquals(0xAB5, programCounter.read().intValue());
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
		
		assertEquals(address, target.readStackTopValue());
		assertEquals(0x567, programCounter.read().intValue());
	}
	
	/**
	 * 3xkk - SE Vx, byte
	 * 
	 * Skip next instruction if Vx = kk. The interpreter compares register Vx to kk, and if they are equal, increments the program counter by 2.
	 */
	@Test
	public void skipNextInstructionSinceDataRegisterValueAndLowestByteEqual() {
		executeOpCode(0x3421);

		assertEquals(address + 4, programCounter.read().intValue());
	}
	
	/**
	 * 3xkk - SE Vx, byte
	 * 
	 * Skip next instruction if Vx = kk. The interpreter compares register Vx to kk, and if they are equal, increments the program counter by 2.
	 */
	@Test
	public void doNotSkipNextInstructionSinceDataRegisterValueAndLowestByteNotEqual() {
		executeOpCode(0x339E);
		
		assertEquals(address + 2, programCounter.read().intValue());
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
		
		assertEquals(address + 4, programCounter.read().intValue());
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

		assertEquals(address + 2, programCounter.read().intValue());
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
		
		assertEquals(address + 4, programCounter.read().intValue());
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
		
		assertEquals(address + 2, programCounter.read().intValue());
	}
	
	/**
	 * 6xkk - LD Vx, byte
	 * 
	 * Set Vx = kk. The interpreter puts the value kk into register Vx.
	 */
	@Test
	public void storeLowestByteIntoDataRegister() {
		executeOpCode(0x63DA);
		
		assertEquals(0xDA, dataRegisters.get(0x3).read().intValue());
	}
	
	/**
	 * 7xkk - ADD Vx, byte
	 * 
	 * Set Vx = Vx + kk. Adds the value kk to the value of register Vx, then stores the result in Vx. 
	 */
	@Test
	public void addsLowestByteToDataRegister() {
		executeOpCode(0x7398);
		
		assertEquals((dataRegisterValues[0x3] + 0x98) & FIT_8BIT_REGISTER, dataRegisters.get(0x3).read().intValue());
	}
	
	/**
	 * 8xy0 - LD Vx, Vy
	 * 
	 * Set Vx = Vy. Stores the value of register Vy in register Vx.
	 */
	@Test
	public void storeDataRegisterValueInAnotherDataRegister() {
		executeOpCode(0x8DC0);
		
		assertEquals(dataRegisters.get(0xC).read().intValue(), dataRegisters.get(0xD).read().intValue());
	}
	
	/**
	 * 8xy1 - OR Vx, Vy
	 * 
	 * Set Vx = Vx OR Vy. Performs a bitwise OR on the values of Vx and Vy, then stores the result in Vx.
	 */
	@Test
	public void storeBitwiseORedDataRegisterValuesInDataRegister() {
		int result = dataRegisters.get(0x3).read() | dataRegisters.get(0x4).read();
		executeOpCode(0x8341);
		
		assertEquals(result, dataRegisters.get(0x3).read().intValue());
	}
	
	/**
	 * 8xy2 - AND Vx, Vy
	 * 
	 * Set Vx = Vx AND Vy. Performs a bitwise AND on the values of Vx and Vy, then stores the result in Vx.
	 */
	@Test
	public void storeBitwiseANDedDataRegisterValuesInDataRegister() {
		int result = dataRegisterValues[0x3] & dataRegisterValues[0x4];
		executeOpCode(0x8342);
		
		assertEquals(result, dataRegisters.get(0x3).read().intValue());
	}
	
	/**
	 * 8xy3 - XOR Vx, Vy
	 * 
	 * Set Vx = Vx XOR Vy. Performs a bitwise exclusive OR on the values of Vx and Vy, then stores the result in Vx.
	 */
	@Test
	public void storeBitwiseXORedDataRegisterValuesInDataRegister() {
		int result = dataRegisters.get(0xD).read() ^ dataRegisters.get(0x5).read();
		executeOpCode(0x85D3);
		
		assertEquals(result, dataRegisters.get(0x5).read().intValue());
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
		
		assertEquals(1, dataRegisters.get(0xF).read().intValue());
		assertEquals((dataRegisterValues[0x1] + dataRegisterValues[0x3]) & FIT_8BIT_REGISTER, dataRegisters.get(0x1).read().intValue());
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
		
		assertEquals(0, dataRegisters.get(0xF).read().intValue());
		assertEquals((dataRegisterValues[0x4] + dataRegisterValues[0x2]) & FIT_8BIT_REGISTER, dataRegisters.get(0x4).read().intValue());
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
		
		assertEquals(0, dataRegisters.get(0xF).read().intValue());
		assertEquals((convertToUnsignedInt(dataRegisterValues[0x2] - dataRegisterValues[0x3]) & FIT_8BIT_REGISTER), dataRegisters.get(0x2).read().intValue());
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
		
		assertEquals(1, dataRegisters.get(0xF).read().intValue());
		assertEquals((dataRegisterValues[0x3] - dataRegisterValues[0x2]) & FIT_8BIT_REGISTER, dataRegisters.get(0x3).read().intValue());
	}
	
	/**
	 * 8xy6 - SHR Vx {, Vy}
	 * 
	 * Set Vx = Vx SHR 1. If the least-significant bit of Vx is 1, then VF is set to 1, otherwise 0. Then Vx is divided by 2.
	 */
	@Test
	public void setCarryAndShiftRight() {
		executeOpCode(0x8456);
		
		assertEquals(1, dataRegisters.get(0xF).read().intValue());
		assertEquals(dataRegisterValues[0x4] >> 1, dataRegisters.get(0x4).read().intValue());
	}
	
	/**
	 * 8xy6 - SHR Vx {, Vy}
	 * 
	 * Set Vx = Vx SHR 1. If the least-significant bit of Vx is 1, then VF is set to 1, otherwise 0. Then Vx is divided by 2.
	 */
	@Test
	public void doNotSetCarryAndShiftRight() {
		executeOpCode(0x8AB6);
		
		assertEquals(0, dataRegisters.get(0xF).read().intValue());
		assertEquals(dataRegisterValues[0xA] >> 1, dataRegisters.get(0xA).read().intValue());
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
		
		assertEquals(1, dataRegisters.get(0xF).read().intValue());
		assertEquals(convertToUnsignedInt(dataRegisterValues[0x1] - dataRegisterValues[0x0]) & FIT_8BIT_REGISTER, dataRegisters.get(0x0).read().intValue());
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
		
		assertEquals(0, dataRegisters.get(0xF).read().intValue());
		assertEquals(convertToUnsignedInt(dataRegisterValues[0x0] - dataRegisterValues[0x1]) & FIT_8BIT_REGISTER, dataRegisters.get(0x1).read().intValue());
	}
	
	/**
	 * 8xyE - SHL Vx {, Vy}
	 * 
	 * Set Vx = Vx SHL 1. If the most-significant bit of Vx is 1, then VF is set to 1, otherwise to 0. Then Vx is multiplied by 2.
	 */
	@Test
	public void setDataRegisterToZeroSinceMostSignificantBitIsNotOne() {
		executeOpCode(0x8ABE);
		
		assertEquals(0, dataRegisters.get(0xF).read().intValue());
		assertEquals((dataRegisterValues[0xA] << 1) & FIT_8BIT_REGISTER, dataRegisters.get(0xA).read().intValue());
	}
	
	/**
	 * 8xyE - SHL Vx {, Vy}
	 * 
	 * Set Vx = Vx SHL 1. If the most-significant bit of Vx is 1, then VF is set to 1, otherwise to 0. Then Vx is multiplied by 2.
	 */
	@Test
	public void setDataRegisterToOneSinceMostSignificantBitIsOne() {
		executeOpCode(0x835E);
		
		assertEquals(1, dataRegisters.get(0xF).read().intValue());
		assertEquals((dataRegisterValues[0x3] << 1) & FIT_8BIT_REGISTER, dataRegisters.get(0x3).read().intValue());
	}
	
	/**
	 * 9xy0 - SNE Vx, Vy
	 * 
	 * Skip next instruction if Vx != Vy. The values of Vx and Vy are compared, and if they are not equal, the program counter is increased by 2.
	 */
	@Test
	public void skipNextInstructionSinceDataRegisterValuesNotEqual() {
		executeOpCode(0x95C0);
		
		assertEquals(address + 4, programCounter.read().intValue());
	}
	
	/**
	 * 9xy0 - SNE Vx, Vy
	 * 
	 * Skip next instruction if Vx != Vy. The values of Vx and Vy are compared, and if they are not equal, the program counter is increased by 2.
	 */
	@Test
	public void doNotSkipNextInstructionSinceDataRegisterValuesAreEqual() {
		executeOpCode(0x9460);
		
		assertEquals(address + 2, programCounter.read().intValue());
	}
	
	/**
	 * Annn - LD I, addr
	 * 
	 * Set I = nnn. The value of the index register is set to nnn.
	 */
	@Test
	public void storeAddressInIndexRegister() {
		executeOpCode(0xAEBA);
		
		assertEquals(0xEBA, indexRegister.read().intValue());
	}
	
	/**
	 * Bnnn - JP V0, addr
	 * 
	 * Jump to location nnn + V0. The program counter is set to nnn plus the value of V0.
	 */
	@Test
	public void setProgramCounterToAddressPlusDataRegisterValue() {
		executeOpCode(0xB348);

		assertEquals(0x348 + dataRegisters.get(0).read(), programCounter.read().intValue());
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
		
		assertEquals(randomNumberGenerator.value() & 0x23, dataRegisters.get(0x0).read().intValue());
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
		writeToMemory(index, 0xF0);
		writeToMemory(index+1, 0x10);
		writeToMemory(index+2, 0xF0);
		writeToMemory(index+3, 0x80);
		writeToMemory(index+4, 0xF0);
		executeOpCode(0xD475);
		
		for (int row = 0; row < 0x5; row++) {
			int coordinateY = dataRegisterValues[0x7] + row;
			int memoryByte = target.getPrimaryMemory().read(index + row);
			for (int column = 0; column < 8; column++) {
				int coordinateX = dataRegisterValues[0x4] + column;
				if ((memoryByte & (0x80 >> column)) != 0) {
					assertTrue(target.getDisplayBuffer().read(convertToIndex(coordinateX, coordinateY)) != 0);
				} else {
					assertTrue(target.getDisplayBuffer().read(convertToIndex(coordinateX, coordinateY)) == 0);
				}
			}
		}
		assertEquals(0, dataRegisters.get(0xF).read().intValue());
		assertTrue(drawFlag.isActive());
	}
	
	private int convertToIndex(int coordinateX, int coordinateY) {
		return (coordinateX % target.getScreen().width()) + ((coordinateY % target.getScreen().width()) * target.getScreen().width());
	}
	
	/**
	 * Ex9E - SKP Vx
	 * 
	 * Skip next instruction if key with the value of Vx is pressed. Checks the keyboard, and if the key corresponding to the
	 * value of Vx is currently in the down position, the program counter is increased by 2.
	 */
	@Test
	public void doNotSkipNextInstructionBecauseKeyEqualToDataRegisterValueIsNotPressed() {
		target.getKeyboard().pressKey(KeyCode.R);
		executeOpCode(0xEA9E);
		
		assertFalse(target.getKeyboard().getCurrentlyPressedKey() == dataRegisters.get(0xA).read().intValue());
		assertEquals(address + 2, programCounter.read().intValue());
	}
	
	/**
	 * Ex9E - SKP Vx
	 * 
	 * Skip next instruction if key with the value of Vx is pressed. Checks the keyboard, and if the key corresponding to the
	 * value of Vx is currently in the down position, the program counter is increased by 4.
	 */
	@Test
	public void skipNextInstructionBecauseKeyEqualToDataRegisterValueIsPressed() {
		target.getKeyboard().pressKey(KeyCode.R);
		executeOpCode(0xED9E);
		
		assertEquals(target.getKeyboard().getCurrentlyPressedKey(), dataRegisters.get(0xD).read().intValue());
		assertEquals(address + 4, programCounter.read().intValue());
	}
	
	/**
	 * ExA1 - SKNP Vx
	 * 
	 * Skip next instruction if key with the value of Vx is not pressed. Checks the keyboard, and if the key corresponding to 
	 * the value of Vx is currently in the up position, the program counter is increased by 4.
	 */
	@Test
	public void skipNextInstructionBecauseKeyEqualToDataRegisterValueIsNotPressed() {
		target.getKeyboard().pressKey(KeyCode.R);
		executeOpCode(0xEAA1);
		
		assertFalse(target.getKeyboard().getCurrentlyPressedKey() == dataRegisters.get(0xA).read().intValue());
		assertEquals(address + 4, programCounter.read().intValue());
	}
	
	/**
	 * ExA1 - SKNP Vx
	 * 
	 * Skip next instruction if key with the value of Vx is not pressed. Checks the keyboard, and if the key corresponding to 
	 * the value of Vx is currently in the up position, the program counter is increased by 2.
	 */
	@Test
	public void doNotSkipNextInstructionBecauseKeyEqualToDataRegisterValueIsPressed() {
		target.getKeyboard().pressKey(KeyCode.R);
		executeOpCode(0xEDA1);
		
		assertEquals(target.getKeyboard().getCurrentlyPressedKey(), dataRegisters.get(0xD).read().intValue());
		assertEquals(address + 2, programCounter.read().intValue());
	}
	
	/**
	 * Fx07 - LD Vx, DT
	 * 
	 * Set Vx = delay timer value. The value of the delay timer is placed into Vx.
	 */
	@Test
	public void storeDelayTimerValueInDataRegister() {
		executeOpCode(0xF207);
		
		assertEquals(delayTimer.currentValue(), dataRegisters.get(0x2).read());
	}
	
	/**
	 * Fx0A - LD Vx, K
	 * 
	 * Wait for a key press, store the value of the key in Vx. All execution stops until a key is pressed, then the value of 
	 * that key is stored in Vx.
	 */
	@Test
	public void valueOfPressedKeyStoredInDataRegister() {
		target.getKeyboard().pressKey(KeyCode.A);
		executeOpCode(0xF70A);
		
		assertEquals(target.getKeyboard().getCurrentlyPressedKey(), dataRegisters.get(0x7).read().intValue());
	}
	
	/**
	 * Fx15 - LD DT, Vx
	 * 
	 * Set delay timer = Vx. Delay timer is set equal to the value of Vx.
	 */
	@Test
	public void setDelayTimerEqualToDataRegisterValue() {
		executeOpCode(0xF615);
		
		assertEquals(dataRegisters.get(0x6).read(), delayTimer.currentValue());
	}
	
	/**
	 * Fx18 - LD ST, Vx
	 * 
	 * Set sound timer = Vx. Sound timer is set equal to the value of Vx, unless the value of Vx is 1. Then the sound
	 * timer is set to 2 (to make the beep last longer).
	 */
	@Test
	public void setSoundTimerEqualToDataRegisterValue() {
		executeOpCode(0xF518);
		
		assertEquals(dataRegisters.get(0x5).read(), soundTimer.currentValue());
	}
	
	/**
	 * Fx18 - LD ST, Vx
	 * 
	 * Set sound timer = Vx. Sound timer is set equal to the value of Vx, unless the value of Vx is 1. Then the sound
	 * timer is set to 2 (to make the sound last longer).
	 */
	@Test
	public void setSoundTimerEqualToTwoSinceDataRegisterValueIsOne() {
		executeOpCode(0xFE18);
		
		assertEquals(1, dataRegisters.get(0xE).read().intValue());
		assertEquals(2, soundTimer.currentValue().intValue());
	}
	
	/**
	 * Fx1E - ADD I, Vx
	 * 
	 * Set I = I + Vx. The values of I and Vx are added, and the result is stored in I. If the result is larger
	 * than 0xFFF, then data register Vf should be set to 1;
	 */
	@Test
	public void storeIndexRegisterPlusDataRegisterValueInIndexRegisterAndSetDataRegisterToZero() {
		executeOpCode(0xFD1E);
		
		assertEquals(0, dataRegisters.get(0xF).read().intValue());
		assertEquals((index + dataRegisterValues[0xD]) & FIT_16BIT_REGISTER, indexRegister.read().intValue());
	}
	
	/**
	 * Fx1E - ADD I, Vx
	 * 
	 * Set I = I + Vx. The values of I and Vx are added, and the result is stored in I. If the result is larger
	 * than 0xFFF, then data register Vf should be set to 1;
	 */
	@Test
	public void storeIndexRegisterPlusDataRegisterValueInIndexRegisterAndSetDataRegisterToOne() {
		index = 0xFFF;
		target.initialize(address, instruction, index, delayTime, soundTime, fontset);
		executeOpCode(0xFD1E);
		
		assertEquals(1, dataRegisters.get(0xF).read().intValue());
		assertEquals((index + dataRegisterValues[0xD]) & FIT_16BIT_REGISTER, indexRegister.read().intValue());
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
		
		assertEquals(dataRegisters.get(0xD).read()*5 & FIT_16BIT_REGISTER, indexRegister.read().intValue());
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

		assertEquals(1, memory.read(index));
		assertEquals(3, memory.read(index+1));
		assertEquals(6, memory.read(index+2));
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
			assertEquals(dataRegisters.get(i).read().intValue(), memory.read(index+i));
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
			assertEquals(memory.read(index+i), dataRegisters.get(i).read().intValue());
		}
	}
}
