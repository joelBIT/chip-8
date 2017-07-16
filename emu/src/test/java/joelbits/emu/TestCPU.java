package joelbits.emu;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javafx.scene.input.KeyCode;
import joelbits.emu.cpu.ALU;
import joelbits.emu.cpu.CPU;
import joelbits.emu.cpu.GPU;
import joelbits.emu.cpu.registers.DataRegister;
import joelbits.emu.cpu.registers.IndexRegister;
import joelbits.emu.cpu.registers.InstructionRegister;
import joelbits.emu.cpu.registers.ProgramCounter;
import joelbits.emu.cpu.registers.Register;
import joelbits.emu.input.Keyboard;
import joelbits.emu.memory.Memory;
import joelbits.emu.memory.RAM;
import joelbits.emu.timers.DelayTimer;
import joelbits.emu.timers.SoundTimer;
import joelbits.emu.timers.Timer;

public class TestCPU {
	@Mock
	private GPU gpu;
	@Mock
	private ALU alu;
	
	private CPU target;
	private Memory primaryMemory;
	private Keyboard keyboard;
	private List<Register<Integer>> dataRegisters;
	private Timer<Integer> delayTimer;
	private Timer<Integer> soundTimer;
	private Stack<Integer> stack;
	private Register<Integer> instructionRegister;
	private Register<Integer> programCounter;
	private Register<Integer> indexRegister;
	
	private final int FIT_8BIT_REGISTER = 0xFF;
	private final int FIT_16BIT_REGISTER = 0xFFFF;
	private int[] dataRegisterValues = {43, 176, 40, 206, 33, 148, 33, 136, 77, 29, 48, 81, 30, 8, 1, 0};
	private int[] fontset = new int[80];
	private int address = 0x200;
	private int instruction = 0x0;
	private int index = 0x250;
	private int delayTime = 0x0;
	private int soundTime = 0x0;
	
	@Before
	public void setUp() {
		dataRegisters = new ArrayList<>();
		for (int i = 0; i <= 0xF; i++) {
			dataRegisters.add(i, new DataRegister<Integer>());
			dataRegisters.get(i).write(dataRegisterValues[i]);
		}
		delayTimer = new DelayTimer<Integer>();
		soundTimer = new SoundTimer<Integer>();
		instructionRegister = InstructionRegister.getInstance();
		programCounter = ProgramCounter.getInstance();
		indexRegister = IndexRegister.getInstance();
		primaryMemory = new RAM();
		keyboard = new Keyboard();
		stack = new Stack<Integer>();
		
		initMocks(this);
		
		target = new CPU(stack, primaryMemory, keyboard, dataRegisters, instructionRegister, programCounter, indexRegister, delayTimer, soundTimer, alu, gpu);
		target.initialize(address, instruction, index, delayTime, soundTime, fontset);
		
	}
	
	/**
	 * 00E0 - CLS
	 * 
	 * Clear the display.
	 */
	@Test
	public void clearBuffers() {
		reset(gpu);
		executeOpCode(0x00E0);

		verify(gpu, times(1)).clearBuffers();
	}
	
	private void executeOpCode(int opcode) {
		writeToMemory(address, (opcode >> 8) & FIT_8BIT_REGISTER);
		writeToMemory(address + 1, opcode & FIT_8BIT_REGISTER);
		
		target.executeNextOperation();
	}
	
	private void writeToMemory(int location, int data) {
		primaryMemory.write(location, data);
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
		assertTrue(stack.peek().equals(address));
		
		writeToMemory(0x567, 0x0);
		writeToMemory(0x568, 0xEE);
		executeOpCode(0x00EE);
		
		assertTrue(stack.empty());
		assertTrue(programCounter.read().equals(address+2));
	}
	
	/**
	 * 1nnn - JP addr
	 * 
	 * Jump to location nnn. The interpreter sets the program counter to nnn.
	 */
	@Test
	public void storeAddressInProgramCounter() {
		executeOpCode(0x1AB5);
		
		assertTrue(programCounter.read().equals(0xAB5));
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
		
		assertTrue(stack.peek().equals(address));
		assertTrue(programCounter.read().equals(0x567));
	}
	
	/**
	 * 3xkk - SE Vx, byte
	 * 
	 * Skip next instruction if Vx = kk. The interpreter compares register Vx to kk, and if they are equal, increments the program counter by 2.
	 */
	@Test
	public void skipNextInstructionIfEqual() {
		executeOpCode(0x3421);

		verify(alu, times(1)).skipNextIfEqual(eq(dataRegisters.get(0x4)), eq(0x21));
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
		
		verify(alu, times(1)).skipNextIfNotEqual(eq(dataRegisters.get(0x3)), eq(0x9E));
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
		
		verify(alu, times(1)).skipNextIfEqual(eq(dataRegisters.get(0x1)), eq(dataRegisters.get(0x8).read()));
	}
	
	/**
	 * 6xkk - LD Vx, byte
	 * 
	 * Set Vx = kk. The interpreter puts the value kk into register Vx.
	 */
	@Test
	public void storeLowestByteIntoDataRegister() {
		executeOpCode(0x63DA);
		
		verify(alu, times(1)).load(eq(dataRegisters.get(0x3)), eq(0xDA));
	}
	
	/**
	 * 7xkk - ADD Vx, byte
	 * 
	 * Set Vx = Vx + kk. Adds the value kk to the value of register Vx, then stores the result in Vx. 
	 */
	@Test
	public void addsLowestByteToDataRegister() {
		executeOpCode(0x7398);
		
		verify(alu, times(1)).add(eq(dataRegisters.get(0x3)), eq(0x98));
	}
	
	/**
	 * 8xy0 - LD Vx, Vy
	 * 
	 * Set Vx = Vy. Stores the value of register Vy in register Vx.
	 */
	@Test
	public void storeDataRegisterValueInAnotherDataRegister() {
		executeOpCode(0x8DC0);
		
		verify(alu, times(1)).load(eq(dataRegisters.get(0xD)), eq(dataRegisters.get(0xC).read()));
	}
	
	/**
	 * 8xy1 - OR Vx, Vy
	 * 
	 * Set Vx = Vx OR Vy. Performs a bitwise OR on the values of Vx and Vy, then stores the result in Vx.
	 */
	@Test
	public void storeBitwiseORedDataRegisterValuesInDataRegister() {
		executeOpCode(0x8341);
		
		verify(alu, times(1)).bitwiseOR(eq(dataRegisters.get(0x3)), eq(dataRegisters.get(0x4).read()));
	}
	
	/**
	 * 8xy2 - AND Vx, Vy
	 * 
	 * Set Vx = Vx AND Vy. Performs a bitwise AND on the values of Vx and Vy, then stores the result in Vx.
	 */
	@Test
	public void storeBitwiseANDedDataRegisterValuesInDataRegister() {
		executeOpCode(0x8342);
		
		verify(alu, times(1)).bitwiseAND(eq(dataRegisters.get(0x3)), eq(dataRegisters.get(0x4).read()));
	}
	
	/**
	 * 8xy3 - XOR Vx, Vy
	 * 
	 * Set Vx = Vx XOR Vy. Performs a bitwise exclusive OR on the values of Vx and Vy, then stores the result in Vx.
	 */
	@Test
	public void storeBitwiseXORedDataRegisterValuesInDataRegister() {
		executeOpCode(0x85D3);
		
		verify(alu, times(1)).bitwiseXOR(eq(dataRegisters.get(0x5)), eq(dataRegisters.get(0xD).read()));
	}
	
	/**
	 * 8xy4 - ADD Vx, Vy
	 * 
	 * Set Vx = Vx + Vy, set VF = carry. The values of Vx and Vy are added together. If the result is greater than 8 bits (i.e., > 255,) VF is
	 * set to 1, otherwise 0. Only the lowest 8 bits of the result are kept, and stored in Vx.
	 */
	@Test
	public void performAdditionWithCarry() {
		executeOpCode(0x8424);
		
		verify(alu, times(1)).addWithCarry(eq(dataRegisters.get(0x4)), eq(dataRegisters.get(0x2).read()), eq(FIT_8BIT_REGISTER));
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
		
		verify(alu, times(1)).subtractWithBorrow(eq(dataRegisters.get(0x3)), eq(dataRegisters.get(0x2).read()));
	}
	
	/**
	 * 8xy6 - SHR Vx {, Vy}
	 * 
	 * Set Vx = Vx SHR 1. If the least-significant bit of Vx is 1, then VF is set to 1, otherwise 0. Then Vx is divided by 2.
	 */
	@Test
	public void shiftRight() {
		executeOpCode(0x8AB6);
		
		verify(alu, times(1)).rightShiftWithCarry(eq(dataRegisters.get(0xA)));
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
		
		verify(alu, times(1)).subtractWithNegatedBorrow(eq(dataRegisters.get(0x1)), eq(dataRegisters.get(0x0).read()));
	}
	
	/**
	 * 8xyE - SHL Vx {, Vy}
	 * 
	 * Set Vx = Vx SHL 1. If the most-significant bit of Vx is 1, then VF is set to 1, otherwise to 0. Then Vx is multiplied by 2.
	 */
	@Test
	public void leftShift() {
		executeOpCode(0x835E);

		verify(alu, times(1)).leftShiftWithCarry(eq(dataRegisters.get(0x3)));
	}
	
	/**
	 * 9xy0 - SNE Vx, Vy
	 * 
	 * Skip next instruction if Vx != Vy. The values of Vx and Vy are compared, and if they are not equal, the program counter is increased by 2.
	 */
	@Test
	public void skipNextInstructionIfDataRegisterValuesNotEqual() {
		executeOpCode(0x95C0);
		
		verify(alu, times(1)).skipNextIfNotEqual(eq(dataRegisters.get(0x5)), eq(dataRegisters.get(0xC).read()));
	}
	
	/**
	 * Annn - LD I, addr
	 * 
	 * Set I = nnn. The value of the index register is set to nnn.
	 */
	@Test
	public void storeAddressInIndexRegister() {
		executeOpCode(0xAEBA);
		
		verify(alu, times(1)).load(eq(indexRegister), eq(0xEBA));
	}
	
	/**
	 * Bnnn - JP V0, addr
	 * 
	 * Jump to location nnn + V0. The program counter is set to nnn plus the value of V0.
	 */
	@Test
	public void setProgramCounterToAddressPlusDataRegisterValue() {
		executeOpCode(0xB348);

		assertTrue(programCounter.read().equals(0x348 + dataRegisters.get(0).read()));
	}
	
	/**
	 * Cxkk - RND Vx, byte
	 * 
	 * Set Vx = random byte AND kk. The interpreter generates a random number from 0 to 255, which is then ANDed with the value kk. 
	 * The results are stored in Vx.
	 */
	@Test
	public void performAdditionUsingRandomNumber() {
		executeOpCode(0xC023);
		
		verify(alu, times(1)).addWithRandom(eq(dataRegisters.get(0x0)), eq(0x23));
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
	public void drawSprite() {
		executeOpCode(0xD475);
		
		verify(gpu, times(1)).drawSprite(eq(dataRegisters), eq(primaryMemory), eq(indexRegister), eq(0xD475));
	}
	
	/**
	 * Ex9E - SKP Vx
	 * 
	 * Skip next instruction if key with the value of Vx is pressed. Checks the keyboard, and if the key corresponding to the
	 * value of Vx is currently in the down position, the program counter is increased by 4.
	 */
	@Test
	public void skipNextInstructionBecauseKeyEqualToDataRegisterValueIsPressed() {
		keyboard.press(KeyCode.R);
		executeOpCode(0xED9E);
		
		verify(alu, times(1)).skipNextIfEqual(eq(dataRegisters.get(0xD)), eq(keyboard.currentlyPressed()));
	}
	
	/**
	 * ExA1 - SKNP Vx
	 * 
	 * Skip next instruction if key with the value of Vx is not pressed. Checks the keyboard, and if the key corresponding to 
	 * the value of Vx is currently in the up position, the program counter is increased by 2.
	 */
	@Test
	public void doNotSkipNextInstructionBecauseKeyEqualToDataRegisterValueIsPressed() {
		keyboard.press(KeyCode.R);
		executeOpCode(0xEDA1);
		
		verify(alu, times(1)).skipNextIfNotEqual(eq(dataRegisters.get(0xD)), eq(keyboard.currentlyPressed()));
	}
	
	/**
	 * Fx07 - LD Vx, DT
	 * 
	 * Set Vx = delay timer value. The value of the delay timer is placed into Vx.
	 */
	@Test
	public void storeDelayTimerValueInDataRegister() {
		executeOpCode(0xF207);
		
		verify(alu, times(1)).load(eq(dataRegisters.get(0x2)), eq(delayTimer.currentValue()));
	}
	
	/**
	 * Fx0A - LD Vx, K
	 * 
	 * Wait for a key press, store the value of the key in Vx. All execution stops until a key is pressed, then the value of 
	 * that key is stored in Vx.
	 */
	@Test
	public void valueOfPressedKeyStoredInDataRegister() {
		keyboard.press(KeyCode.A);
		executeOpCode(0xF70A);
		
		verify(alu, times(1)).load(eq(dataRegisters.get(0x7)), eq(keyboard.currentlyPressed()));
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
		
		assertTrue(dataRegisters.get(0xE).read().equals(1));
		assertTrue(soundTimer.currentValue().equals(2));
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
		
		verify(alu, times(1)).addWithCarry(eq(indexRegister), eq(dataRegisters.get(0xD).read()), eq(index));
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
		
		verify(alu, times(1)).load(eq(indexRegister), eq(dataRegisters.get(0xD).read()*5 & FIT_16BIT_REGISTER));
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

		assertEquals(1, primaryMemory.read(index));
		assertEquals(3, primaryMemory.read(index+1));
		assertEquals(6, primaryMemory.read(index+2));
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
			assertTrue(dataRegisters.get(i).read().equals(primaryMemory.read(index+i)));
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
			assertEquals(primaryMemory.read(index+i), dataRegisters.get(i).read().intValue());
		}
	}
}
