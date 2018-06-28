package joelbits.emulator.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import joelbits.emulator.utils.Chip8Util;

public class UtilModule extends AbstractModule {
    @Override
    protected void configure() { }

    @Provides
    public Chip8Util getChip8Util() {
        return new Chip8Util();
    }
}
