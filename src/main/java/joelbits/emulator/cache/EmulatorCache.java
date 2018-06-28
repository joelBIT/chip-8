package joelbits.emulator.cache;

import javafx.scene.canvas.GraphicsContext;
import joelbits.emulator.Emulator;

public final class EmulatorCache {
    private static GraphicsContext graphics;
    private static Emulator emulator;

    private EmulatorCache() { }

    public static void setEmulator(Emulator emul) {
        emulator = emul;
    }

    public static Emulator getEmulator() {
        return emulator;
    }

    public static void setGraphicsContext(GraphicsContext graphicsContext) {
        graphics = graphicsContext;
    }

    public static GraphicsContext getGraphicsContext() {
        return graphics;
    }

    public static boolean hasGraphicsContext() {
        return graphics != null;
    }

    public static boolean hasEmulator() {
        return emulator != null;
    }
}
