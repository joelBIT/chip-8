package joelbits.emulator.units;

import java.util.List;

import joelbits.emulator.cpu.registers.Register;
import joelbits.emulator.flags.Flag;
import joelbits.emulator.memory.Memory;
import joelbits.emulator.output.Screen;

public class GPU {
	private final Memory displayBuffer;
	private final Memory dirtyBuffer;
	private final Flag drawFlag;
	private final Flag clearFlag;
	private final Screen<Integer> screen;
	
	public GPU(Memory displayBuffer, Memory dirtyBuffer, Screen<Integer> screen, Flag drawFlag, Flag clearFlag) {
		this.displayBuffer = displayBuffer;
		this.dirtyBuffer = dirtyBuffer;
		this.drawFlag = drawFlag;
		this.clearFlag = clearFlag;
		this.screen = screen;
	}
	
	void drawScreen() {
		int dirtyBufferSize = dirtyBuffer.size();
		int screenWidth = screen.width();
		for (int i = 0; i < dirtyBufferSize; i++) {
			int dirtyLocation = dirtyBuffer.read(i);
			int coordinateX = dirtyLocation % screenWidth;
			int coordinateY = dirtyLocation / screenWidth;
			if (displayBuffer.read(dirtyLocation) != 0) {
				screen.fill(coordinateX, coordinateY);
			} else {
				screen.clear(coordinateX, coordinateY);
			}
		}
	}
	
	public void drawSprite(List<Register<Integer>> dataRegisters, Memory primaryMemory, Register<Integer> indexRegister, int instruction) {
		int nibble = instruction & 0x000F;
		int registerLocationX = (instruction & 0x0F00) >> 8;
		int registerLocationY = (instruction & 0x00F0) >> 4;
		
		dataRegisters.get(0xF).write(0);
		for (int row = 0; row < nibble; row++) {
			int memoryByte = primaryMemory.read(indexRegister.read() + row);
			int coordinateY = dataRegisters.get(registerLocationY).read() + row;
			for (int column = 0; column < 8; column++) {
				if ((memoryByte & (0x80 >> column)) != 0) {
					int coordinateX = dataRegisters.get(registerLocationX).read() + column;
					int data = displayBuffer.read(convertToIndex(coordinateX, coordinateY));
					if (data != 0) {
						dataRegisters.get(0xF).write(1);
					}
					displayBuffer.write(convertToIndex(coordinateX, coordinateY), data^1);
					dirtyBuffer.write(convertToIndex(coordinateX, coordinateY), data^1);
				}
			}
		}
		if (!drawFlag.isActive()) {
			drawFlag.toggle();
		}
	}
	
	private int convertToIndex(int coordinateX, int coordinateY) {
		return (coordinateX % screen.width()) + ((coordinateY % screen.width()) * screen.width());
	}
	
	void clearScreen() {
		int displayBufferSize = displayBuffer.size();
		int screenWidth = screen.width();
		for (int i = 0; i < displayBufferSize; i++) {
			int coordinateX = i % screenWidth;
			int coordinateY = i / screenWidth;
			screen.clear(coordinateX, coordinateY);
		}
	}
	
	public void clearBuffers() {
		dirtyBuffer.clear();
		displayBuffer.clear();
		if (!clearFlag.isActive()) {
			clearFlag.toggle();
		}
	}
}
