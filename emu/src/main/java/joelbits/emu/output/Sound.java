package joelbits.emu.output;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Generate a pulse-code modulation encoded sound that uses a (default system) mixer as its output source.
 * 
 * @author rollnyj
 *
 */
public class Sound {
	private boolean isEnabled;
    private boolean isPlaying;
    private AudioFormat audioFormat;
    private SourceDataLine sourceDataLine;
    private final int SOUND_BUFFER_SIZE = 256;
    private byte[] soundBuffer = new byte[SOUND_BUFFER_SIZE];
	    
    public Sound() {
    	audioFormat = new AudioFormat(8000f, 8, 1, false, false);
    	try {
    		sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
			sourceDataLine.open(audioFormat);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

    	for (int i = soundBuffer.length/3; i < 2*soundBuffer.length/3; i++) {
        	soundBuffer[i] = (byte) (SOUND_BUFFER_SIZE - 12);
    	}
    }

    public void startSound() {
    	if (isPlaying || !isEnabled) {
        	return;
        }
    	isPlaying = true;
    	new Thread(new SoundThread()).start();
    }
	    
    public void stopSound() {
    	isPlaying = false;
    }

    public void setEnabled(boolean isEnabled) {
    	this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
    	return isEnabled;
    }

    class SoundThread implements Runnable {

    	@Override
    	public void run() {
    		try {
            	sourceDataLine.start();
            	while (isPlaying) {
            		sourceDataLine.write(soundBuffer, 0, soundBuffer.length);
            	}
            	sourceDataLine.stop();
            	sourceDataLine.flush();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
}
