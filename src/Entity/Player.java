package Entity;

import audio.AudioManager;
import Input.KeyInputs;
import Main.GameEngine;
import Main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Player {

    private int x;
    private int y;

    // --- CONTROLE DE VELOCIDADE PROGRESSIVA ---
    private final int VELOCIDADE_INICIAL = 3; // Começa com essa velocidade
    private final int VELOCIDADE_MAXIMA = 15; // Limite para não ficar injogável/bugado
    private int velocidadeAtual = VELOCIDADE_INICIAL;

    // --- VARIÁVEIS DE FÍSICA E COLISÃO ---
    private final double CHAO_Y = 801.0; // A coordenada de colisão/chão
    private double velocidadeY = 0.0;     // <--- Velocidade vertical (gravidade e salto)

    // --- REFERÊNCIAS ---
    private final GamePanel gamePanel;
    private final GameEngine gameEngine;
    KeyInputs keyH;

    // --- SISTEMA DE SPRITES ---
    private BufferedImage idleSpriteSheet;
    private BufferedImage runSpriteSheet;
    private BufferedImage[] idleSprites;
    private BufferedImage[] runSprites;
    private BufferedImage currentSprite;

    // --- CONTROLE DE ANIMAÇÃO ---
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private final long ANIMATION_VELOCIDADE = 100;
    private final int SPRITE_SIZE = 32;
    private int idleTotalFrames = 0;
    private int runTotalFrames = 0;

    // --- POSIÇÃO E VELOCIDADE ---
    private int worldX = 0;
    public int getWorldX() { return worldX; }

    private AnimationState currentState = AnimationState.IDLE;
    private AnimationState previousState = AnimationState.IDLE;



    public enum AnimationState {
        IDLE,
        RUN
    }

    // Posição na tela (calculada)
    private int playerX = 100, playerY = 800;
    private final int Velocidade = 2; // <--- Velocidade Horizontal (lateral)

    public Player(GameEngine gameEngine, GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.gameEngine = gameEngine;
        loadSprites();
        lastFrameTime = System.currentTimeMillis();
    }

    public int getSpeedX() {
        if (keyH == null) return 0;
        if (keyH.rightPressed) return Velocidade;
        if (keyH.leftPressed) return -Velocidade;
        return 0;
    }

    private void loadSprites() {
        try {
            runSpriteSheet = ImageIO.read(getClass().getResourceAsStream("/res/RUN.png"));
            if (runSpriteSheet != null) {
                runTotalFrames = runSpriteSheet.getWidth() / SPRITE_SIZE;
                runSprites = new BufferedImage[runTotalFrames];
                for (int i = 0; i < runTotalFrames; i++) {
                    runSprites[i] = runSpriteSheet.getSubimage(i * SPRITE_SIZE, 0, SPRITE_SIZE, SPRITE_SIZE);
                }
            }

            if (idleSprites != null && idleSprites.length > 0) currentSprite = idleSprites[0];

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateAnimation() {
        if (currentState != previousState) {
            currentFrame = 0;
            previousState = currentState;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= ANIMATION_VELOCIDADE) {
            BufferedImage[] activeSprites;
            int totalFrames;

            if (currentState == AnimationState.RUN) {
                activeSprites = runSprites;
                totalFrames = runTotalFrames;
            } else {
                activeSprites = idleSprites;
                totalFrames = idleTotalFrames;
            }

            if (activeSprites != null && totalFrames > 0) {
                currentFrame = (currentFrame + 1) % totalFrames;
                currentSprite = activeSprites[currentFrame];
                lastFrameTime = currentTime;
            }
        }
    }

    private void updateAnimationState() {
        boolean isMoving = keyH != null && (keyH.rightPressed || keyH.leftPressed);
        currentState = isMoving ? AnimationState.RUN : AnimationState.IDLE;
    }

    public void setKeyInputs(KeyInputs keyH) {
        this.keyH = keyH;
    }

    public BufferedImage getSprite() { return currentSprite; }

    // ====================================================================
    // MOVIMENTO E FÍSICA PRINCIPAL
    // ====================================================================

    public void update() {
        if (keyH == null) return;

        // Força animação de corrida
        currentState = AnimationState.RUN;
        updateAnimation();

        // ----------------------------------------------------
        // 1. CÁLCULO DA VELOCIDADE PROGRESSIVA
        // ----------------------------------------------------

        // Calcula metros (WorldX / 20)
        int metrosPercorridos = worldX / 20;

        // Calcula o "Nível" de velocidade (Sua lógica: a cada 500m)
        int bonusVelocidade = metrosPercorridos / 500;

        // Define a nova velocidade
        velocidadeAtual = VELOCIDADE_INICIAL + bonusVelocidade;

        // Trava na velocidade máxima
        if (velocidadeAtual > VELOCIDADE_MAXIMA) {
            velocidadeAtual = VELOCIDADE_MAXIMA;
        }

        // ----------------------------------------------------
        // 2. MOVIMENTO AUTOMÁTICO
        // ----------------------------------------------------
        worldX += velocidadeAtual;

        if (worldX < 0) worldX = 0;

        // ----------------------------------------------------
        // 3. GRAVIDADE, PULO E DESCIDA RÁPIDA (FAST FALL)
        // ----------------------------------------------------

        // Se estiver no ar (acima do chão)
        if ((double)playerY < CHAO_Y) {
            velocidadeY += 0.5; // Gravidade Normal

            // --- NOVO: FAST FALL (Descer rápido) ---
            // Se apertar para baixo (S) enquanto cai, desce muito mais rápido
            if (keyH.downPressed) {
                velocidadeY += 1.5f;
            }
            // ---------------------------------------

        } else if ((double)playerY > CHAO_Y) {
            // Correção se passar do chão
            playerY = (int) CHAO_Y;
            velocidadeY = 0.0;
        }

        // Calcula posição futura
        double proximaY = playerY + velocidadeY;

        // Verifica colisão (anti-tunneling)
        double yCorrigido = verificarColisaoY((double) playerY, proximaY);

        // Aplica posição Y
        playerY = (int) Math.round(yCorrigido);

        // --- NOVO: PULO MAIS ALTO ---
        // Se apertar Pulo e estiver no chão
        if (keyH.upPressed && (double)playerY == CHAO_Y) {
            // Aumentado de -15.0 para -18.0
            velocidadeY = -18.0;
        }
    }

    /**
     * Verifica se o movimento de 'yAtual' para 'yProxima' cruzou o CHAO_Y (801.0).
     * Trata o problema de "tunneling" e zera a velocidade vertical.
     * @param yAtual A posição Y antes da aplicação da velocidade.
     * @param yProxima A posição Y depois da aplicação da velocidade.
     * @return A nova posição Y, corrigida para o limite (CHAO_Y) em caso de colisão.
     */
    private double verificarColisaoY(double yAtual, double yProxima) {
        // Se o player estava ACIMA do chão (yAtual < CHAO_Y) E cruzou para BAIXO (yProxima >= CHAO_Y)
        if (yAtual < CHAO_Y && yProxima >= CHAO_Y) {

            // **IMPORTANTE:** Zera a velocidade vertical para parar a queda.
            this.velocidadeY = 0.0;

            // Retorna o valor exato do chão para que playerY pare em 801.
            return CHAO_Y;

        } else {
            // Nenhuma colisão ou o player já está no ar.
            return yProxima;
        }
    }

    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }

    public double getGameTime() {
        return gameEngine.getElapsedGameTimeSeconds();
    }
    public Rectangle getBounds() {
        // TAMANHO DO SPRITE: 128x128
        // TAMANHO DO INIMIGO: 64x64

        // Queremos que a hitbox pegue apenas a METADE DE BAIXO do player
        // para garantir que ele bata de frente com o inimigo pequeno.

        int larguraHitbox = 64;
        int alturaHitbox = 64; // Mesma altura do inimigo

        // OFFSET X: Centraliza horizontalmente no sprite de 128
        int offsetX = (128 - larguraHitbox) / 2; // = 32

        // OFFSET Y: Empurra para baixo.
        // Se o sprite tem 128 e a hitbox tem 64, sobram 64 pixels.
        // Somamos 64 ao Y para a caixa descer até o pé.
        int offsetY = 128 - alturaHitbox; // = 64

        // RETORNO FINAL
        return new Rectangle(worldX + offsetX, playerY + offsetY, larguraHitbox, alturaHitbox);
    }

    public void resetTimer() {
        // Zera o tempo na engine
        gameEngine.resetTimer();

        // --- MUDANÇA: Zera a distância e reseta a velocidade ---
        this.worldX = 0;
        this.velocidadeAtual = VELOCIDADE_INICIAL;
        this.playerY = 800; // Reseta posição Y para o chão (ajuste se seu chão for diferente)
    }

    public int getVelocidadeAtual() {
        // Retorna a velocidade que calculamos no passo anterior
        return this.velocidadeAtual;
    }

    // ====================================================================
    // LÓGICA DE MORTE E SOM
    // ====================================================================

    public void checkDeath() {
        // 1. Para a música da fase imediatamente
        AudioManager.stopBackgroundMusic();

        // 2. Toca o som de morte
        // Nota: Se você criou o método 'playSound' (sem loop) que sugeri antes, use ele aqui.
        // Caso contrário, use playBackgroundMusic mesmo.
        AudioManager.playGameOverMusic("death.wav");
    }

}