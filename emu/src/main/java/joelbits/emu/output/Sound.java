package joelbits.emu.output;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;

public final class Sound implements Startable, Mutable {
    private final int SAMPLE_RATE = 8000;
    private final int SAMPLE_SIZE_IN_BITS = 8;
    private final byte[] soundBytes;
    private final AudioFormat audioFormat;
    private Clip beepSound;
    private boolean beeping;
    private boolean muted;

    public Sound() {
    	soundBytes = initializeSoundBytes();
    	audioFormat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, 1, true, false);
    	
    	try {
    		beepSound = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, audioFormat));
        	beepSound.open(initializeAudioInputStream());
        } catch (LineUnavailableException | IOException e) {
        	e.printStackTrace();
        	beepSound.close();
        }
    }
    
    private byte[] initializeSoundBytes() {
    	final int SOUND_FREQUENCY = 880;
    	final int SOUND_VOLUME = 30;
    	
    	byte[] soundBytes = new byte[SAMPLE_RATE / SAMPLE_SIZE_IN_BITS];
    	for (int i = 0; i < SAMPLE_RATE / SAMPLE_SIZE_IN_BITS; i++) {
    		soundBytes[i] = (byte) ((double)SOUND_VOLUME * Math.sin(6.28 * (double)SOUND_FREQUENCY * (i / (double)SAMPLE_RATE)));
    	}
    	
    	return soundBytes;
    }
	
	private AudioInputStream initializeAudioInputStream() {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(soundBytes);
		return new AudioInputStream(byteArrayInputStream, audioFormat, SAMPLE_RATE / SAMPLE_SIZE_IN_BITS);

	}

    @Override
    public void start() {
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
    public void stop() {
    	if (isBeeping() && !isMuted()) {
    		beepSound.stop();
    	}
    	beeping = false;
    }

    @Override
    public void mute() {
    	if (isBeeping()) {
    		beepSound.stop();
    	}
    	muted = true;
    }

    @Override
    public void unmute() {
    	if (isBeeping() && isMuted()) {
        	beepSound.loop(-1);
    	}
    	muted = false;
    }
}
