package joelbits.emulator.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import javafx.scene.input.KeyCode;
import joelbits.emulator.config.InterpreterConfig;
import joelbits.emulator.flags.ClearFlag;
import joelbits.emulator.flags.DrawFlag;
import joelbits.emulator.flags.Flag;
import joelbits.emulator.input.Input;
import joelbits.emulator.input.Keyboard;
import joelbits.emulator.memory.Memory;
import joelbits.emulator.memory.RAM;
import joelbits.emulator.output.Audio;
import joelbits.emulator.output.Sound;
import joelbits.emulator.settings.GameSettings;
import joelbits.emulator.timers.DelayTimer;
import joelbits.emulator.timers.SoundTimer;
import joelbits.emulator.timers.Timer;
import joelbits.emulator.graphics.GMU;

public final class InterpreterModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(new TypeLiteral<Input<Integer, KeyCode>>() {}).to(Keyboard.class).in(Scopes.SINGLETON);
		bind(GameSettings.class).asEagerSingleton();
		bind(Audio.class).to(Sound.class).in(Scopes.SINGLETON);
		bind(Memory.class).to(RAM.class);
		bind(GMU.class).in(Scopes.SINGLETON);
        bind(Flag.class).annotatedWith(Names.named("clear")).to(ClearFlag.class).asEagerSingleton();
        bind(Flag.class).annotatedWith(Names.named("draw")).to(DrawFlag.class).asEagerSingleton();
		bind(new TypeLiteral<Timer<Integer>>() {}).annotatedWith(Names.named("sound")).to(new TypeLiteral<SoundTimer<Integer>>() {});
		bind(new TypeLiteral<Timer<Integer>>() {}).annotatedWith(Names.named("delay")).to(new TypeLiteral<DelayTimer<Integer>>() {});
	}

	@Provides
	public InterpreterConfig getInterpreterConfig() {
		return new InterpreterConfig();
	}
}
