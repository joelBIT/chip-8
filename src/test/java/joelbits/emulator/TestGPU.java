package joelbits.emulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import joelbits.emulator.memory.RAM;
import joelbits.emulator.output.Chip8Screen;
import joelbits.emulator.units.MMU;
import org.junit.Before;
import org.junit.Test;

import joelbits.emulator.units.GPU;
import joelbits.emulator.cpu.registers.DataRegister;
import joelbits.emulator.cpu.registers.IndexRegister;
import joelbits.emulator.cpu.registers.Register;
import joelbits.emulator.flags.ClearFlag;
import joelbits.emulator.flags.DrawFlag;
import joelbits.emulator.flags.Flag;
import joelbits.emulator.memory.BufferFactory;
import joelbits.emulator.memory.Memory;
import joelbits.emulator.output.Screen;

public class TestGPU {
	private GPU target;
	private MMU mmu;
	private Memory displayBuffer;
	private Memory dirtyBuffer;
	private List<Register<Integer>> dataRegisters;
	private Flag drawFlag;
	private Flag clearFlag;
	private Register<Integer> indexRegister;

	private int[] dataRegisterValues = {43, 176, 40, 206, 33, 148, 33, 136, 77, 29, 48, 81, 30, 8, 1, 0};
	private int SCREEN_WIDTH = 64;
	private int SCREEN_HEIGHT = 32;
	private int PIXEL_SIZE = 14;
	
	@Before
	public void setUp() {
		dataRegisters = new ArrayList<>();
		populateRegisters(dataRegisterValues);
		drawFlag = new DrawFlag();
		clearFlag = new ClearFlag();
		indexRegister = IndexRegister.getInstance();
		Screen<Integer> screen = new Chip8Screen(SCREEN_WIDTH, SCREEN_HEIGHT, PIXEL_SIZE);
		displayBuffer = BufferFactory.createDisplayBuffer(SCREEN_WIDTH, SCREEN_HEIGHT);
		dirtyBuffer = BufferFactory.createDirtyBuffer();
		mmu = new MMU(new RAM());
		
		target = new GPU(displayBuffer, dirtyBuffer, screen, drawFlag, clearFlag);
	}
	
	private void populateRegisters(int[] registerValues) {
		for (int i = 0; i < registerValues.length; i++) {
			dataRegisters.add(i, new DataRegister<>());
			dataRegisters.get(i).write(dataRegisterValues[i]);
		}
	}
	
	@Test
	public void clearBuffers() {
		displayBuffer.write(0x345, 0x59);
		dirtyBuffer.write(0x345, 0x59);
		
		target.clearBuffers();
		
		for (int i = 0; i < SCREEN_HEIGHT * SCREEN_WIDTH; i++) {
			assertEquals(0x0, displayBuffer.read(i));
		}
		assertEquals(0, dirtyBuffer.size());
		assertTrue(clearFlag.isActive());
	}

	@Test
	public void storeValuesStartingAtIndexRegisterLocationIntoDataRegistersWithoutCollision() {
		initializeSpriteDrawing();
		int[] addresses = new int[]{0x221,0x222,0x223,0x224,0x264};
		
		target.drawSprite(dataRegisters, mmu.primaryMemory(), indexRegister, 0xD475);
		
		assertDirtyBuffer(dirtyBuffer, addresses);
		assertDisplayBuffer(displayBuffer, addresses, -1);

		assertEquals(0, (int) dataRegisters.get(0xF).read());
		assertTrue(drawFlag.isActive());
	}
	
	private void initializeSpriteDrawing() {
		indexRegister.write(0x200);
		writeToMemory(indexRegister.read(), 0xF0);
		writeToMemory(indexRegister.read()+1, 0x10);
	}
	
	private void writeToMemory(int location, int data) {
		mmu.writePrimaryMemory(location, data);
	}
	
	private void assertDirtyBuffer(Memory dirtyBuffer, int[] addresses) {
		int size = dirtyBuffer.size();
		for (int i = 0; i < size; i++) {
			assertEquals(addresses[i], dirtyBuffer.read(0));
		}
		assertEquals(0, dirtyBuffer.size());
	}
	
	private void assertDisplayBuffer(Memory displayBuffer, int[] addresses, int collisionAddress) {
		for (int address : addresses) {
			int value = address != collisionAddress ? 1 : 0;
			assertEquals(value, displayBuffer.read(address));
		}
	}
	
	@Test
	public void storeValuesStartingAtIndexRegisterLocationIntoDataRegistersWithCollision() {
		initializeSpriteDrawing();
		int[] addresses = new int[]{0x221,0x222,0x223,0x224,0x264};
		int COLLISION_ADDRESS = 0x223;
		displayBuffer.write(COLLISION_ADDRESS, 0x1);
		
		target.drawSprite(dataRegisters, mmu.primaryMemory(), indexRegister, 0xD475);
		
		assertDirtyBuffer(dirtyBuffer, addresses);
		assertDisplayBuffer(displayBuffer, addresses, COLLISION_ADDRESS);

		assertEquals(1, (int) dataRegisters.get(0xF).read());
		assertTrue(drawFlag.isActive());
	}
}
