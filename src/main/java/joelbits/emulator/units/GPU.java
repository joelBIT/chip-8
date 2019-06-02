package joelbits.emulator.units;

import java.util.List;

import joelbits.emulator.cpu.registers.Register;
import joelbits.emulator.flags.Flag;
import joelbits.emulator.memory.Memory;
import joelbits.emulator.output.Screen;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GPU {
	private final Memory displayBuffer;
	private final Memory dirtyBuffer;
	private final Screen<Integer> screen;
	private final Flag drawFlag;
	private final Flag clearFlag;

	/**
	 * Updates all affected sprites on the screen. To avoid redrawing sprites that are in the same state as
	 * before, only dirty sprites are updated. The dirty buffer contains information about which sprites has
	 * had their state changed and thus should be redrawn.
	 */
	void drawScreen() {
		for (int i = 0; i < dirtyBuffer.size(); i++) {
			int dirtyLocation = dirtyBuffer.read(i);
			int x = dirtyLocation % screen.width();
			int y = dirtyLocation / screen.width();
			updateSprite(displayBuffer.read(dirtyLocation), x, y);
		}
	}

	/**
	 * Updates sprite on the screen. If value is 0 then clear sprite, otherwise
	 * draw sprite on supplied coordinates.
	 *
	 * @param value		value from dirty buffer
	 * @param x			x coordinate of sprite
	 * @param y			y coordinate of sprite
	 */
	private void updateSprite(int value, int x, int y) {
		if (value != 0) {
			screen.fill(x, y);
		} else {
			screen.clear(x, y);
		}
	}

	public void drawSprite(List<Register<Integer>> dataRegisters, int[] primaryMemory, Register<Integer> indexRegister, int instruction) {
		int nibble = instruction & 0x000F;
		int registerLocationX = (instruction & 0x0F00) >> 8;
		int registerLocationY = (instruction & 0x00F0) >> 4;
		
		dataRegisters.get(0xF).write(0);
		for (int row = 0; row < nibble; row++) {
			int memoryByte = primaryMemory[indexRegister.read() + row];
			int coordinateY = dataRegisters.get(registerLocationY).read() + row;
			for (int column = 0; column < 8; column++) {
				if ((memoryByte & (0x80 >> column)) != 0) {
					int coordinateX = dataRegisters.get(registerLocationX).read() + column;
					updateBuffers(dataRegisters, coordinateX, coordinateY);
				}
			}
		}
		activateDrawFlag();
	}

	private void updateBuffers(List<Register<Integer>> dataRegisters, int coordinateX, int coordinateY) {
		int data = displayBuffer.read(convertToIndex(coordinateX, coordinateY));
		if (data != 0) {
			dataRegisters.get(0xF).write(1);
		}
		displayBuffer.write(convertToIndex(coordinateX, coordinateY), data^1);
		dirtyBuffer.write(convertToIndex(coordinateX, coordinateY), data^1);
	}

	private int convertToIndex(int coordinateX, int coordinateY) {
		return (coordinateX % screen.width()) + ((coordinateY % screen.width()) * screen.width());
	}

	/**
	 * Activate draw flag to notify Chip8 that the sprite should be drawn on next occurrence
	 * of the draw instruction.
	 */
	private void activateDrawFlag() {
		if (!drawFlag.isActive()) {
			drawFlag.toggle();
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
