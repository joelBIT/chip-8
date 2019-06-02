package joelbits.emulator.output;

import javafx.scene.canvas.GraphicsContext;
import joelbits.emulator.cache.EmulatorCache;

public class Chip8Screen extends Screen<Integer> {
    private final GraphicsContext graphicsContext = EmulatorCache.getInstance().getGraphicsContext();

    public Chip8Screen(Integer width, Integer height, Integer pixelSize) {
        super(width, height, pixelSize);
    }

    @Override
    public void fill(double x, double y) {
        graphicsContext.fillRect(x* getPixelSize(), y* getPixelSize(), getPixelSize(), getPixelSize());
    }

    @Override
    public void clear(double x, double y) {
        graphicsContext.clearRect(x* getPixelSize(), y* getPixelSize(), getPixelSize(), getPixelSize());
    }

    @Override
    public void clearAll(int bufferSize) {
        for (int i = 0; i < bufferSize; i++) {
            int coordinateX = i % getWidth();
            int coordinateY = i / getWidth();
            graphicsContext.clearRect(coordinateX* getPixelSize(), coordinateY* getPixelSize(), getPixelSize(), getPixelSize());
        }
    }
}
