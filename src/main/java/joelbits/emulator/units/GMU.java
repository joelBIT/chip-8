package joelbits.emulator.units;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.scene.canvas.GraphicsContext;
import joelbits.emulator.config.InterpreterConfig;
import joelbits.emulator.flags.Flag;
import joelbits.emulator.memory.BufferFactory;
import joelbits.emulator.memory.Memory;
import joelbits.emulator.modules.ModuleFactory;
import joelbits.emulator.output.Screen;

/**
 * Graphics Management Unit. Handles tasks related to graphics.
 */
public class GMU {
    private final GPU gpu;
    @Inject
    @Named("clear")
    private Flag clearFlag;
    @Inject
    @Named("draw")
    private Flag drawFlag;
    @Inject
    private InterpreterConfig config;

    public GMU(GraphicsContext graphicsContext) {
        Guice.createInjector(ModuleFactory.gmuModule()).injectMembers(this);

        Memory displayBuffer = BufferFactory.createDisplayBuffer(config.screenWidth(), config.screenHeight());
        Memory dirtyBuffer = BufferFactory.createDirtyBuffer();
        Screen<Integer> screen = new Screen<>(config.screenWidth(), config.screenHeight(), config.pixelSize());
        gpu = new GPU(displayBuffer, dirtyBuffer, screen, graphicsContext, drawFlag, clearFlag);
    }

    public GPU gpu() {
        return gpu;
    }

    public void clearScreen() {
        gpu.clearScreen();
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
