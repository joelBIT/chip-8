package joelbits.emu.output;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;

/**
 * Generate a pulse-code modulation encoded sound that uses a (default system) mixer as its output source.
 * 
 * @author rollnyj
 *
 */
public class Beep implements Sound {
    private final int SAMPLE_RATE = 8000;
    private final int SAMPLE_SIZE_IN_BITS = 8;
    private final int SOUND_FREQUENCY = 880;
    private final int SOUND_VOLUME = 30;
    private final byte[] soundBytes = new byte[SAMPLE_RATE / SAMPLE_SIZE_IN_BITS];
    private Clip beepSound;
    private ByteArrayInputStream byteArrayInputStream;
    private AudioFormat audioFormat;
    private AudioInputStream audioInputStream;
    private DataLine.Info dataLineInfo;
    private boolean beeping;
    private boolean muted;

    public Beep() {
    	for (int i = 0; i < SAMPLE_RATE / SAMPLE_SIZE_IN_BITS; i++) {
    		soundBytes[i] = (byte) ((double)SOUND_VOLUME * Math.sin(6.283185307179586 * (double)SOUND_FREQUENCY * (i / (double)SAMPLE_RATE)));
    	}
    	byteArrayInputStream = new ByteArrayInputStream(soundBytes);
    	audioFormat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, 1, true, false);
    	audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, SAMPLE_RATE / SAMPLE_SIZE_IN_BITS);
    	dataLineInfo = new DataLine.Info(Clip.class, audioFormat);
    	try {
        	beepSound = (Clip) AudioSystem.getLine(dataLineInfo);
            beepSound.open(audioInputStream);
        } catch (LineUnavailableException | IOException e) {
        	e.printStackTrace();
        }
    }

    @Override
    public synchronized void startSound() {
    	if (!isBeeping() && !isMuted()) {
        	beepSound.loop(-1);
        }
        beeping = true;
    }
    
    private boolean isBeeping() {
        return beeping;
    }
    
    private boolean isMuted() {
        return muted;
    }

    @Override
    public synchronized void stopSound() {
        if (isBeeping() && !isMuted()) {
            beepSound.stop();
        }
        beeping = false;
    }

    @Override
    public synchronized void muteSound() {
        if (isBeeping()) {
            beepSound.stop();
        }
        muted = true;
    }

    @Override
    public synchronized void unmuteSound() {
        if (isBeeping() && isMuted()) {
        	beepSound.loop(-1);
        }
        muted = false;
    }
}
