package audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.InputStream;

public class SoundEffect {

    public static void play(String fileName) {
        try {
            // Caminho atualizado (res_audio/audio/)
            InputStream audioSrc = SoundEffect.class.getResourceAsStream("/audio/" + fileName);

            if (audioSrc == null) {
                System.out.println("Som não encontrado: " + fileName);
                return;
            }

            AudioInputStream audioStream =
                    AudioSystem.getAudioInputStream(new java.io.BufferedInputStream(audioSrc));

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Aumentar volume
            try {
                FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                volume.setValue(6.0f); // aumento de volume
            } catch (Exception e) {
                System.out.println("Controle de volume não suportado.");
            }

            clip.start();

            // Espera o som terminar
            while (clip.isRunning()) {
                Thread.sleep(50);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



