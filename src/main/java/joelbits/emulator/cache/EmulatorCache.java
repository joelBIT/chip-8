package joelbits.emulator.cache;

import com.google.inject.Injector;
import javafx.scene.canvas.GraphicsContext;
import joelbits.emulator.Emulator;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class EmulatorCache {
    private GraphicsContext graphicsContext;
    private Emulator emulator;
    private Injector injector;
    private static EmulatorCache cache;

    private EmulatorCache() { }

    public static EmulatorCache getInstance() {
        if (Objects.isNull(cache)) {
            cache = new EmulatorCache();
        }
        return cache;
    }

    public boolean hasGraphicsContext() {
        return Objects.nonNull(graphicsContext);
    }

    public boolean hasEmulator() {
        return Objects.nonNull(emulator);
    }
}
