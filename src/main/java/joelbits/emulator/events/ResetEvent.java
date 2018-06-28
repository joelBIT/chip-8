package joelbits.emulator.events;

import javafx.event.Event;
import javafx.event.EventHandler;
import joelbits.emulator.Chip8;
import joelbits.emulator.Emulator;
import joelbits.emulator.cache.EmulatorCache;
import joelbits.emulator.units.GMU;

public class ResetEvent implements EventHandler {
    @Override
    public void handle(Event event) {
        if (EmulatorCache.hasEmulator()) {
            EmulatorCache.getEmulator().reset();
        } else if (EmulatorCache.hasGraphicsContext()) {
            Emulator emulator = new Chip8(new GMU(EmulatorCache.getGraphicsContext()));
            EmulatorCache.setEmulator(emulator);
            emulator.reset();
        }
    }
}
