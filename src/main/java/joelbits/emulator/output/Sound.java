package joelbits.emulator.output;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;

import static joelbits.emulator.utils.Chip8Util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Sound implements Audio {
	private static final Logger log = LoggerFactory.getLogger(Sound.class);
    private final AudioFormat audioFormat;
    private Clip beepSound;
    private boolean beeping;
    private boolean muted;

    public Sound() {
    	audioFormat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, true, false);
    	
    	try {
    		beepSound = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, audioFormat));
        	beepSound.open(createAudioInputStream());
        } catch (LineUnavailableException | IOException e) {
        	log.error(e.toString(), e);
			if(Objects.nonNull(beepSound) && beepSound.isOpen()) {
				beepSound.close();
			}
		}
    }
    
	private AudioInputStream createAudioInputStream() {
		return new AudioInputStream(new ByteArrayInputStream(soundBytes()), audioFormat, SAMPLE_RATE / SAMPLE_SIZE_IN_BITS);
	}
	
    private byte[] soundBytes() {
    	byte[] soundBytes = new byte[SAMPLE_RATE / SAMPLE_SIZE_IN_BITS];
    	for (int i = 0; i < SAMPLE_RATE / SAMPLE_SIZE_IN_BITS; i++) {
    		soundBytes[i] = (byte) ((double)SOUND_VOLUME * Math.sin(6.28 * (double)SOUND_FREQUENCY * (i / (double)SAMPLE_RATE)));
    	}
    	
    	return soundBytes;
    }

    @Override
    public void start() {
    	if (!beeping && !muted) {
    		beepSound.loop(-1);
        }
    	beeping = true;
    }

    @Override
    public void stop() {
    	if (beeping && !muted) {
    		beepSound.stop();
    	}
    	beeping = false;
    }

    @Override
    public void mute() {
    	if (beeping) {
    		beepSound.stop();
    	}
    	muted = true;
    }

    @Override
    public void unmute() {
    	if (beeping && muted) {
        	beepSound.loop(-1);
    	}
    	muted = false;
    }
}
