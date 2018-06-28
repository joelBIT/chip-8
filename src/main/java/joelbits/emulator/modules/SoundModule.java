package joelbits.emulator.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import joelbits.emulator.output.Audio;
import joelbits.emulator.output.Sound;

public final class SoundModule extends AbstractModule {
    private static final Audio sound = new Sound();

    @Override
    protected void configure() { }

    @Provides
    public Audio getAudio() {
        return sound;
    }
}
