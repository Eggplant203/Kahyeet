import javax.sound.sampled.*;
import java.io.File;

/**
 * Sound class handles the loading and playing of sound files.
 */
public class Sound {
    private Clip clip;

    /**
     * Constructor to load a sound file.
     * @param filePath The path to the sound file.
     */
    public Sound(String filePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("sound/" + filePath).getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays the sound once.
     */
    public void playOnce() {
        if (clip != null) {
            clip.setFramePosition(0); // Reset to the beginning
            clip.start();
        }
    }

    /**
     * Plays the sound in a continuous loop.
     */
    public void playLoop() {
        if (clip != null) {
            clip.setFramePosition(0); // Reset to the beginning
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop continuously
            clip.start();
        }
    }

    /**
     * Stops playing the sound.
     */
    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }
}