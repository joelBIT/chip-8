package joelbits.emulator.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import joelbits.emulator.config.InterpreterConfig;
import joelbits.emulator.gui.components.ComponentCreator;
import joelbits.emulator.utils.Chip8Util;

public final class ComponentModule extends AbstractModule {
    @Override
    protected void configure() { }

    @Provides
    public ComponentCreator getComponentCreator() {
        return new ComponentCreator();
    }

    @Provides
    public Chip8Util getChip8Util() {
        return new Chip8Util();
    }

    @Provides
    public InterpreterConfig getInterpreterConfig() {
        return new InterpreterConfig();
    }
}
