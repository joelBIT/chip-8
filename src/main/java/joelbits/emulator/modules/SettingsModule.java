package joelbits.emulator.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import joelbits.emulator.settings.GameSettings;

public final class SettingsModule extends AbstractModule {
    private static final GameSettings settings = new GameSettings();

    @Override
    protected void configure() { }

    @Provides
    public GameSettings getSettings() {
        return settings;
    }
}
