package Main;

import Entity.EnemyFlying;
import Entity.Player;
import Entity.Enemy;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random; // Import necessário

public class GamePanel extends JPanel {

    private boolean gameOver = false; // O jogo começa rodando (falso)

    private GameMap gameMap;
    private int cameraX = 0;

    private ArrayList<Enemy> enemies;
    private ArrayList<EnemyFlying> flyingEnemies;
    private Player player;

    // --- VARIÁVEIS DE GERAÇÃO DE INIMIGOS ---
    private Random random;
    private int tickCounter = 0; // Conta as atualizações do jogo
    private int tempoParaProximoSpawn; // Quanto tempo falta para o próximo inimigo

    public static final int LARGURA_TELA = 1920;
    public static final int ALTURA_TELA = 1080;

    public GamePanel() {
        this.setPreferredSize(new Dimension(LARGURA_TELA, ALTURA_TELA));
        this.setBackground(Color.BLACK);

        gameMap = new GameMap();
        enemies = new ArrayList<>();
        flyingEnemies = new ArrayList<>();

        // Inicializa o gerador aleatório
        random = new Random();
        tempoParaProximoSpawn = 60; // Começa criando um inimigo em 1 segundo (60 ticks)
    }

    public void update() {
        // 0. Se o jogo acabou, para tudo
        if (gameOver) return;

        // 1. ATUALIZA O PLAYER PRIMEIRO
        // O player anda e define o novo WorldX
        player.update();

        // 2. ATUALIZA A CÂMERA DO MAPA
        // A câmera segue o novo WorldX do player
        gameMap.update(player.getWorldX(), getWidth());

        // Pega a câmera atualizada para usar na limpeza de inimigos
        int cameraAtual = gameMap.getCameraX();

        // 3. ATUALIZA INIMIGOS TERRESTRES
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            e.update();

            // Remove se sair da tela (ficou pra trás)
            if (e.getX() + e.getWidth() < cameraAtual - 100) {
                enemies.remove(i);
                i--;
            }

            for (Enemy enemy : enemies) {
                enemy.update();

                // VERIFICA SE O PLAYER BATEU NO INIMIGO
                if (player.getBounds().intersects(enemy.getBounds())) {

                    // Chama o método que criamos no Player
                    player.checkDeath();
                }
            }
        }

        // 4. ATUALIZA INIMIGOS VOADORES (NOVO)
        // Precisamos atualizar a lista nova também!
        for (int i = 0; i < flyingEnemies.size(); i++) {
            EnemyFlying f = flyingEnemies.get(i);
            f.update();

            // Remove se sair da tela
            if (f.getX() + f.getWidth() < cameraAtual - 100) {
                flyingEnemies.remove(i);
                i--;
            }
        }

        // 5. LÓGICA DE SPAWN INTELIGENTE
        tickCounter++;

        if (tickCounter >= tempoParaProximoSpawn) {
            spawnInimigo();
            tickCounter = 0; // Reseta o contador

            // --- CÁLCULO DINÂMICO DE TEMPO (VERSÃO MAIS ALEATÓRIA) ---
            int velocidadePlayer = player.getVelocidadeAtual();
            if (velocidadePlayer <= 0) velocidadePlayer = 1;

            // 1. Distância Mínima:
            // Reduzi para 250. Isso permite que inimigos venham mais perto um do outro,
            // exigindo reflexos mais rápidos do jogador.
            int distanciaMinima = 250;

            // 2. Variação (O Segredo da Aleatoriedade):
            int variacao = random.nextInt(400);

            // Fórmula: Tempo = Distância / Velocidade
            tempoParaProximoSpawn = (distanciaMinima + variacao) / velocidadePlayer;
        }

        // 6. CHECA TODAS AS COLISÕES
        checkColisao();
    }


    private void spawnInimigo() {
        int cameraAtual = gameMap.getCameraX();
        int spawnX = cameraAtual + LARGURA_TELA + 100; // Nasce fora da tela

        // 1. Calcula a distância atual em metros
        int metrosPercorridos = player.getWorldX() / 20;

        // 2. Decide se vai criar um Voador
        // Regra Aprimorada:
        // - Se passou de 1000m: 50% de chance (random.nextBoolean())
        // - Se for menos de 1000m: 10% de chance surpresa (random.nextInt(100) < 10)
        boolean chanceNormal = (metrosPercorridos >= 1000) && random.nextBoolean();
        boolean chanceSurpresa = (metrosPercorridos > 200) && (random.nextInt(100) < 10); // Raro, mas possível

        boolean criarVoador = chanceNormal || chanceSurpresa;

        if (criarVoador) {
            // ALTURA ALEATÓRIA (entre 600 e 700)
            int min = 600;
            int max = 700;
            int alturaVoador = min + random.nextInt(max - min + 1);

            flyingEnemies.add(new EnemyFlying(spawnX, alturaVoador));
        } else {
            // INIMIGO DE CHÃO
            // Adicionei uma pequena variação no chão também (opcional)
            // Variação de -10 a +10 pixels no eixo X do spawn
            int variacaoX = random.nextInt(20) - 10;

            int chaoY = 800 + (128 - 64);
            enemies.add(new Enemy(spawnX + variacaoX, chaoY));
        }
    }
    public void moveCamera(int amount) {
        cameraX += amount;
    }

    public int getCameraX() {
        return cameraX;
    }

    public void setPlayer(Player player) {
        this.player = player;
        gameMap.setPlayer(player);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Segurança inicial: Se o player não existe, não desenha nada para evitar erro
        if (player == null) return;

        // Pega a câmera OFICIAL do mapa (sincronizada)
        int camX = gameMap.getCameraX();

        // ============================================================
        // 1. CAMADA DO FUNDO (MAPA)
        // ============================================================
        gameMap.draw((Graphics2D) g, getWidth(), getHeight(), player);

        // ============================================================
        // 2. CAMADA DOS INIMIGOS (SPRITES)
        // ============================================================
        for (Enemy enemy : enemies) {
            enemy.draw(g, camX);
        }

        // ============================================================
        // 3. CAMADA DA INTERFACE (UI) - TIMER E PONTUAÇÃO
        // ============================================================
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));

        // --- A. Timer (Tempo de Jogo) ---
        double gameTime = player.getGameTime();
        String timeString = String.format("Tempo: %.2f", gameTime);
        g.drawString(timeString, 20, 30); // Y = 30

        // --- B. Pontuação por Distância ---
        // Divide o WorldX por 20 para transformar pixels em "metros" fictícios
        int distancia = player.getWorldX() / 20;
        String scoreString = "Distância: " + distancia + "m";

        // Desenha logo abaixo do timer (Y = 60)
        g.drawString(scoreString, 20, 60);

        // ============================================================
        // 4. CAMADA DO JOGADOR (SPRITE)
        // ============================================================
        if (player.getSprite() != null) {
            // A MÁGICA: Calculamos a posição na tela AGORA (World - Camera)
            int screenX = player.getWorldX() - camX;

            g.drawImage(
                    player.getSprite(),
                    screenX,
                    player.getPlayerY(),
                    128,
                    128,
                    null
            );
        }

        /*

        // ============================================================
        // 5. MODO DEBUG (VISUALIZAR HITBOXES)
        // ============================================================

        // --- A. Debug do Player (VERMELHO) ---
        g.setColor(Color.RED);

        // Pega a caixa oficial (que usa WorldX)
        Rectangle hitbox = player.getBounds();

        // Converte Mundo -> Tela para desenhar a linha no lugar certo
        int drawX = hitbox.x - camX;
        int drawY = hitbox.y;

        g.drawRect(drawX, drawY, hitbox.width, hitbox.height);

        // --- B. Debug dos Inimigos (AMARELO) --- <--- ADICIONADO AGORA
        g.setColor(Color.YELLOW);

        for (Enemy enemy : enemies) {
            Rectangle eRect = enemy.getBounds();

            // Converte Mundo -> Tela
            int eDrawX = eRect.x - camX;
            int eDrawY = eRect.y;

            g.drawRect(eDrawX, eDrawY, eRect.width, eRect.height);
        }
        */

        // NOVO: Desenha Inimigos Voadores
        for (EnemyFlying f : flyingEnemies) {
            f.draw(g, camX);
        }

        /*
        // NOVO: Debug Amarelo para Voadores (Opcional)
        g.setColor(Color.YELLOW);
        for (EnemyFlying f : flyingEnemies) {
            Rectangle r = f.getBounds();
            g.drawRect(r.x - camX, r.y, r.width, r.height);
        }
        */


        // ============================================================
        // 6. TELA DE GAME OVER (OVERLAY)
        // ============================================================
        if (gameOver) {
            // Escurece a tela levemente (efeito transparente)
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, LARGURA_TELA, ALTURA_TELA);

            // Texto Vermelho
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 100));
            String msg = "GAME OVER";

            // Centraliza o texto (cálculo simples)
            int textWidth = g.getFontMetrics().stringWidth(msg);
            g.drawString(msg, (LARGURA_TELA / 2) - (textWidth / 2), ALTURA_TELA / 2);
        }
    }

    private void checkColisao() {
        Rectangle playerRect = player.getBounds();

        // 1. Checa Chão (Já existe)
        for (Enemy enemy : enemies) {
            if (playerRect.intersects(enemy.getBounds())) {
                gameOver = true;
                audio.SoundEffect.play("death.wav"); // <<< SOM DE MORTE
            }
        }

        // 2. NOVO: Checa Voadores
        for (EnemyFlying f : flyingEnemies) {
            if (playerRect.intersects(f.getBounds())) {
                System.out.println("Bateu no pássaro!");
                gameOver = true;
                audio.SoundEffect.play("death.wav"); // <<< SOM DE MORTE
            }
        }
    }

    // Em GamePanel.java
    public boolean isGameOver() {
        return gameOver;
    }

}