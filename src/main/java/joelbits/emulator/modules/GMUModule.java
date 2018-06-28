package joelbits.emulator.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import joelbits.emulator.flags.ClearFlag;
import joelbits.emulator.flags.DrawFlag;
import joelbits.emulator.flags.Flag;

public final class GMUModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Flag.class).annotatedWith(Names.named("clear")).to(ClearFlag.class);
        bind(Flag.class).annotatedWith(Names.named("draw")).to(DrawFlag.class);
    }

    @Provides
    public InterpreterModule getInterpreterModule() {
        return new InterpreterModule();
    }
}
