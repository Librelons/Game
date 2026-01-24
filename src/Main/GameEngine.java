package Main;

import Entity.Player;
import Input.KeyInputs;

import java.awt.*;
import java.io.Serial;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameEngine extends Canvas implements Runnable {

    private GameWindow gameWindow;
    private GamePanel gamePanel;
    private Player player;
    private KeyInputs keyInputs;


    private long gameStartTime; //MARCOS
    private double elapsedGameTimeSeconds; //MARCOS

    @Serial
    private static final long serialVersionUID = 1L;

    //Controle Main.Game Loop
    private Thread gameThread;
    private volatile boolean running = false;

    //Controle FPS
    public static final int FPS = 60;

    //Intervalo de tempo para cada atualização em segundos.
    public static final float SECONDS_PER_UPDATE = 1.0f / FPS;

    private int playerID;

    public GameEngine(GamePanel gamePanel, int playerID) {
        this.gamePanel = gamePanel;
        // No construtor da GameEngine:
        this.playerID = playerID; // Armazena o ID do jogador

        // Inicializa os componentes do jogo
        this.player = new Player(this, gamePanel);
        this.keyInputs = new KeyInputs(gamePanel);
        // Configura os links
        gamePanel.setPlayer(player);
        player.setKeyInputs(keyInputs);

        // Configura o input no painel
        gamePanel.addKeyListener(keyInputs);
        gamePanel.setFocusable(true);

        // Marca o tempo de início
        gameStartTime = System.nanoTime();
    }

    protected void updatePlayer() {
        // Também trava o player se o jogo acabou
        if (player != null && !gamePanel.isGameOver()) {
            player.update();
        }
    }

    private void update(float secondsPerUpdate) {
        // Se o jogo acabou, NÃO atualiza nada
        if (gamePanel.isGameOver()) {
            return;
        }

        if (gamePanel != null) {
            gamePanel.update();
        }
    }

    protected void render(float interpolation) {
    }

    // Inicia o game loop em uma nova thread
    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // Para o game loop de forma segura
    public synchronized void stop() {
        if (!running) return;
        running = false;

        // 1. Calcula a distância final (igual ao HUD: worldX / 20)
        int distanciaFinal = 0;
        if (player != null) {
            distanciaFinal = player.getWorldX() / 20;
        }

        // 2. Salva ID, Distância e Tempo
        Util.DatabaseConnection.saveScore(this.playerID, distanciaFinal, elapsedGameTimeSeconds);

        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        long lastTime = System.nanoTime();
        double timeAccumulator = 0.0;

        // Variáveis para controle do frame (Capping)
        long frameStartTime;
        long frameDrawTime;
        long timeToSleep;

        // --- Variáveis para o Contador de FPS/UPS ---
        long timer = System.currentTimeMillis();
        int frames = 0;
        int updates = 0;

        while (running) {
            frameStartTime = System.nanoTime(); // Marca o início do frame

            long now = System.nanoTime();
            double elapsedTime = (now - lastTime) / 1000000000.0;
            lastTime = now;

            // Só conta o tempo de jogo se não for Game Over
            if (!gamePanel.isGameOver()) {
                elapsedGameTimeSeconds += elapsedTime;
            }
            timeAccumulator += elapsedTime;

            // Loop de Física (UPS travado em 60)
            while (timeAccumulator >= SECONDS_PER_UPDATE) {
                update(SECONDS_PER_UPDATE);
                updatePlayer();
                timeAccumulator -= SECONDS_PER_UPDATE;
                updates++;
            }

            // Renderização
            render(0);
            gamePanel.repaint();
            frames++;

            // --- EXIBIÇÃO DE FPS/UPS NO TERMINAL ---
            if (System.currentTimeMillis() - timer > 1000) {
                // Imprime no console
                System.out.printf("FPS: %d | UPS: %d | Tempo Jogo: %.2f s%n", frames, updates, elapsedGameTimeSeconds);

                // Reinicia os contadores para o próximo segundo
                frames = 0;
                updates = 0;
                timer += 1000;
            }

            // --- CÓDIGO PARA TRAVAR O FPS EM 60 ---

            // 1. Calcula quanto tempo levou para processar e desenhar este frame
            frameDrawTime = System.nanoTime() - frameStartTime;

            // 2. Define o tempo total que um frame deve durar (aprox 16.6ms)
            long targetTime = (long) (SECONDS_PER_UPDATE * 1000000000);

            // 3. Calcula quanto tempo sobra para dormir
            timeToSleep = targetTime - frameDrawTime;

            if (timeToSleep > 0) {
                try {
                    // Converte de nanosegundos para milissegundos para o sleep
                    Thread.sleep(timeToSleep / 1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // ---------------------------------------
        }
    }

    Graphics2D g2d = (Graphics2D) this.getGraphics();

    public Player getPlayer() {
        return this.player;
    }

    public double getElapsedGameTimeSeconds() {
        return elapsedGameTimeSeconds;
    }

    // Método novo para zerar o tempo manualmente
    public void resetTimer() {
        this.elapsedGameTimeSeconds = 0;
    }

}
