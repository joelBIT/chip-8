package joelbits.emulator.units;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import joelbits.emulator.cache.EmulatorCache;
import joelbits.emulator.config.InterpreterConfig;
import joelbits.emulator.cpu.registers.Register;
import joelbits.emulator.flags.Flag;
import joelbits.emulator.memory.BufferFactory;
import joelbits.emulator.memory.Memory;
import joelbits.emulator.output.Chip8Screen;
import joelbits.emulator.output.Screen;

import java.util.List;

/**
 * Graphics Management Unit. Handles tasks related to graphics.
 */
public class GMU {
    private final GPU gpu;
    private final Memory displayBuffer;
    private final Screen<Integer> screen;
    @Inject
    @Named("clear")
    private Flag clearFlag;
    @Inject
    @Named("draw")
    private Flag drawFlag;
    @Inject
    private InterpreterConfig config;

    public GMU() {
        EmulatorCache.getInstance().getInjector().injectMembers(this);

        displayBuffer = BufferFactory.createDisplayBuffer(config.screenWidth(), config.screenHeight());
        screen = new Chip8Screen(config.screenWidth(), config.screenHeight(), config.pixelSize());
        gpu = new GPU(displayBuffer, BufferFactory.createDirtyBuffer(), screen, drawFlag, clearFlag);
    }

    public void clearScreen() {
        screen.clearAll(displayBuffer.size());
    }

    public void clearBuffers() {
        gpu.clearBuffers();
    }

    public void drawSprite(List<Register<Integer>> dataRegisters, Memory primaryMemory, Register<Integer> indexRegister, int instruction) {
        gpu.drawSprite(dataRegisters, primaryMemory, indexRegister, instruction);
    }

    public void drawScreen() {
        gpu.drawScreen();
    }

    public boolean isClearFlagActive() {
        return clearFlag.isActive();
    }

    public boolean isDrawFlagActive() {
        return drawFlag.isActive();
    }

    public void toggleClearFlag() {
        clearFlag.toggle();
    }

    public void toggleDrawFlag() {
        drawFlag.toggle();
    }
}
