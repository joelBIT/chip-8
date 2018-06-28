package joelbits.emulator.modules;

public final class ModuleFactory {
    private static SoundModule soundModule = new SoundModule();
    private static ComponentModule componentModule = new ComponentModule();
    private static InterpreterModule interpreterModule = new InterpreterModule();
    private static SettingsModule settingsModule = new SettingsModule();

    private ModuleFactory() { }

    public static SoundModule soundModule() {
        return soundModule;
    }

    public static ComponentModule componentModule() {
        return componentModule;
    }

    public static InterpreterModule interpreterModule() {
        return interpreterModule;
    }

    public static SettingsModule settingsModule() {
        return settingsModule;
    }
}
