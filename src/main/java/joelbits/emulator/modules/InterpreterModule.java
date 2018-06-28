package joelbits.emulator.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
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
import joelbits.emulator.timers.DelayTimer;
import joelbits.emulator.timers.SoundTimer;
import joelbits.emulator.timers.Timer;
import joelbits.emulator.utils.Chip8Util;

public final class InterpreterModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Memory.class).to(RAM.class);
		bind(Flag.class).annotatedWith(Names.named("clear")).to(ClearFlag.class);
		bind(Flag.class).annotatedWith(Names.named("draw")).to(DrawFlag.class);
		bind(new TypeLiteral<Timer<Integer>>() {}).annotatedWith(Names.named("sound")).to(new TypeLiteral<SoundTimer<Integer>>() {});
		bind(new TypeLiteral<Timer<Integer>>() {}).annotatedWith(Names.named("delay")).to(new TypeLiteral<DelayTimer<Integer>>() {});
	}

	@Provides
	public Chip8Util getChip8Util() {
		return new Chip8Util();
	}

	@Provides
	public InterpreterConfig interpreterConfig() {
		return new InterpreterConfig();
	}
}
