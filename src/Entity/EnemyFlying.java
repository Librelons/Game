package Entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class EnemyFlying {

    // Posição no Mundo
    private int x, y;
    private int width, height;

    // Sprites
    private BufferedImage[] animationFrames;
    private int aniTick, aniIndex, aniSpeed = 10;
    private int qtdQuadros = 4; // Ajuste conforme sua imagem de pássaro/morcego

    public EnemyFlying(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = 64;
        this.height = 64;
        loadAnimations();
    }

    private void loadAnimations() {
        animationFrames = new BufferedImage[qtdQuadros];
        for (int i = 0; i < qtdQuadros; i++) {
            try {
                // Certifique-se de ter imagens tipo "bat_0.png", "bat_1.png" na pasta res
                String nomeArquivo = "/res/bat_" + i + ".png";
                InputStream is = getClass().getResourceAsStream(nomeArquivo);

                if (is != null) {
                    animationFrames[i] = ImageIO.read(is);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateAnimationTick() {
        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= qtdQuadros) aniIndex = 0;
        }
    }

    public void update() {
        x -= 5; // Voadores geralmente são mais rápidos!
        updateAnimationTick();
    }

    public void draw(Graphics g, int cameraX) {
        int screenX = x - cameraX;

        if (animationFrames != null && animationFrames[0] != null) {
            g.drawImage(animationFrames[aniIndex], screenX, y, width, height, null);
        } else {
            // Debug: Quadrado Roxo se não tiver imagem
            g.setColor(new Color(128, 0, 128));
            g.fillRect(screenX, y, width, height);
        }
    }

    public Rectangle getBounds() {
        // Hitbox um pouco menor para ser justo no ar
        return new Rectangle(x + 10, y + 10, width - 20, height - 20);
    }

    // Getters necessários
    public int getX() { return x; }
    public int getWidth() { return width; }
}