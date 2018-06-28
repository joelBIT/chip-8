package joelbits.emulator.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import javafx.scene.input.KeyCode;
import joelbits.emulator.input.Input;
import joelbits.emulator.input.Keyboard;

public final class KeyboardModule extends AbstractModule {
    private static final Input<Integer, KeyCode> keyboard = new Keyboard();

    @Override
    protected void configure() { }

    @Provides
    public Input<Integer, KeyCode> keyboard() {
        return keyboard;
    }
}
