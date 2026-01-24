package audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.InputStream;

public class AudioManager {

    private static Clip backgroundClip;

    // Tocar música de fundo (menu ou fase)
    public static void playBackgroundMusic(String fileName) {
        try {
            // Se já tiver uma música tocando, para ela antes de começar a nova
            stopBackgroundMusic();

            InputStream audioSrc = AudioManager.class.getResourceAsStream("/res/audio/" + fileName);

            if (audioSrc == null) {
                System.out.println("Música não encontrada: " + fileName);
                return;
            }

            AudioInputStream audioStream =
                    AudioSystem.getAudioInputStream(new java.io.BufferedInputStream(audioSrc));

            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioStream);
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- NOVO MÉTODO: Música de Game Over ---
    public static void playGameOverMusic(String fileName) {
        // 1. Garante que a música da fase pare
        stopBackgroundMusic();

        // 2. Reutiliza o método de tocar música (assim ela fica em loop na tela de Game Over)
        playBackgroundMusic(fileName);
    }

    // Parar música
    public static void stopBackgroundMusic() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
            backgroundClip.close();
        }
    }
}