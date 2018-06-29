package joelbits.emulator.events;

import javafx.event.Event;
import javafx.event.EventHandler;
import joelbits.emulator.Chip8;
import joelbits.emulator.Emulator;
import joelbits.emulator.cache.EmulatorCache;

public class ResetEvent implements EventHandler {
    @Override
    public void handle(Event event) {
        if (EmulatorCache.getInstance().hasEmulator()) {
            EmulatorCache.getInstance().getEmulator().reset();
        } else if (EmulatorCache.getInstance().hasGraphicsContext()) {
            Emulator emulator = new Chip8();
            EmulatorCache.getInstance().setEmulator(emulator);
            emulator.reset();
        }
    }
}
