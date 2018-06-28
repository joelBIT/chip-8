package joelbits.emulator.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import joelbits.emulator.gui.components.ComponentCreator;

public class ComponentModule extends AbstractModule {
    @Override
    protected void configure() { }

    @Provides
    public ComponentCreator getComponentCreator() {
        return new ComponentCreator();
    }
}
