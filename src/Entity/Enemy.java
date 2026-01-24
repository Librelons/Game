package Entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Enemy {

    // Posição e Tamanho no Mundo
    private int x, y;
    private int width, height;

    // --- VARIÁVEIS DE SPRITE E ANIMAÇÃO ---
    private BufferedImage[] animationFrames;
    private int aniTick, aniIndex, aniSpeed = 10;

    private int qtdQuadros = 6;

    public Enemy(int x, int y) {
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
                String nomeArquivo = "/res/run_" + i + ".png";
                InputStream is = getClass().getResourceAsStream(nomeArquivo);

                if (is == null) {
                    System.out.println("Erro: Não achei a imagem " + nomeArquivo);
                    continue;
                }

                animationFrames[i] = ImageIO.read(is);

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
            if (aniIndex >= qtdQuadros) {
                aniIndex = 0;
            }
        }
    }

    public void update() {
        x -= 2; // Velocidade do inimigo
        updateAnimationTick();
    }

    public void draw(Graphics g, int cameraX) {
        int screenX = x - cameraX;

        if (animationFrames != null && animationFrames.length > 0) {
            g.drawImage(animationFrames[aniIndex], screenX, y, width, height, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(screenX, y, width, height);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // --- ADICIONE ESTES MÉTODOS (NECESSÁRIOS PARA O GAMEPANEL) ---
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}