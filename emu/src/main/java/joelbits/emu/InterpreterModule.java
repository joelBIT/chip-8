package joelbits.emu;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import javafx.scene.input.KeyCode;
import joelbits.emu.flags.ClearFlag;
import joelbits.emu.flags.DrawFlag;
import joelbits.emu.flags.Flag;
import joelbits.emu.input.Input;
import joelbits.emu.input.Keyboard;
import joelbits.emu.memory.Memory;
import joelbits.emu.memory.RAM;
import joelbits.emu.timers.DelayTimer;
import joelbits.emu.timers.SoundTimer;
import joelbits.emu.timers.Timer;

public class InterpreterModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Memory.class).to(RAM.class);
		bind(Flag.class).annotatedWith(Names.named("clear")).to(ClearFlag.class);
		bind(Flag.class).annotatedWith(Names.named("draw")).to(DrawFlag.class);
		bind(new TypeLiteral<Timer<Integer>>() {}).annotatedWith(Names.named("sound")).to(new TypeLiteral<SoundTimer<Integer>>() {});
		bind(new TypeLiteral<Timer<Integer>>() {}).annotatedWith(Names.named("delay")).to(new TypeLiteral<DelayTimer<Integer>>() {});
		bind(new TypeLiteral<Input<Integer, KeyCode>>() {}).to(Keyboard.class);
	}

}
