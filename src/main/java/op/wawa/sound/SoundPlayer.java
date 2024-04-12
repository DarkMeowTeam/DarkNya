package op.wawa.sound;

import net.ccbluex.liquidbounce.DarkNya;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;

import static net.ccbluex.liquidbounce.DarkNya.isStarting;

public class SoundPlayer {
    public void playSound(SoundType st,float volume) {
        if (DarkNya.isStarting) return;

        new Thread(() -> {
            AudioInputStream as;
            try {
                as = AudioSystem.getAudioInputStream(new BufferedInputStream(Objects.requireNonNull(this.getClass()
                        .getResourceAsStream("/assets/minecraft/darknya/sound/" + st.getName()))));
                Clip clip = AudioSystem.getClip();
                clip.open(as);
                clip.start();
                FloatControl gainControl =
                        (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(volume); // Reduce volume by 10 decibels.
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public enum SoundType{
        SPECIAL("spec.wav");

        final String name;
        SoundType(String fileName){
            this.name = fileName;
        }
        String getName(){
            return name;
        }
    }
}
