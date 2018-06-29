package joelbits.emulator.cache;

import com.google.inject.Injector;
import javafx.scene.canvas.GraphicsContext;
import joelbits.emulator.Emulator;

public class EmulatorCache {
    private GraphicsContext graphics;
    private Emulator emulator;
    private Injector injector;
    private static EmulatorCache cache;

    private EmulatorCache() { }

    public static EmulatorCache getInstance() {
        if (cache == null) {
            cache = new EmulatorCache();
        }
        return cache;
    }

    public void setEmulator(Emulator emulator) {
        this.emulator = emulator;
    }

    public Emulator getEmulator() {
        return emulator;
    }

    public void setGraphicsContext(GraphicsContext graphicsContext) {
        graphics = graphicsContext;
    }

    public GraphicsContext getGraphicsContext() {
        return graphics;
    }

    public Injector getInjector() { return injector; }

    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public boolean hasGraphicsContext() {
        return graphics != null;
    }

    public boolean hasEmulator() {
        return emulator != null;
    }
}
