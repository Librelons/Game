package Main;

import javax.swing.*;
import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class GameWindow extends JFrame {

    // --- Variáveis ---
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private GameEngine gameEngine;
    private GamePanel gamePanel;
    private LoginPanel loginPanel;


//DA LINHA 20 ATÉ A LINHA 56 SEM FULLSREEN
    public GameWindow() {
        // 1. Configuração da Janela (JFrame)
        super("RunCat 2D"); // Define o título aqui
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Vamos controlar o fechamento
        this.setResizable(true);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH); //FAZ O JOGO INICIAR MAXIMIZADO

        // 2. Configuração do CardLayout para trocar de tela
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // 3. Cria e adiciona o painel de login primeiro
        loginPanel = new LoginPanel(this); // Passa a referência desta janela
        mainContainer.add(loginPanel, "LOGIN");

        // 4. Adiciona o container principal à janela
        this.add(mainContainer);
        //this.pack(); // Ajusta o tamanho ao LoginPanel - ESTÁ COMENTADO PORQUE O JOGO INICIA MAXIMIZADO USANDO this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //this.setLocationRelativeTo(null); //ESTÁ COMENTADO PORQUE O JOGO INICIA MAXIMIZADO USANDO this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setVisible(true);

        // 5. Adiciona o listener de fechamento
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Só tenta salvar se o motor do jogo já foi iniciado
                if (gameEngine != null) {
                    System.out.println("Janela fechando, salvando pontuação...");
                    gameEngine.stop();
                } else {
                    System.out.println("Janela fechada antes de iniciar o jogo.");
                }
                // Fecha a aplicação
                System.exit(0);
            }
        });
    } // --- Fim do Construtor GameWindow() --- SEM ESTAR EM FULLSCREEN

    //DA LINHA 59 ATÉ A LINHA 106 COM FULLSREEN!!
/**
    public GameWindow() {
        // 1. Configuração da Janela
        super("2D SquareGame");
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // --- INÍCIO DO CÓDIGO FULLSCREEN ---

        // Remove a barra de título (Obrigatório para fullscreen)
        this.setUndecorated(true);

        // Pega o dispositivo de tela (monitor principal)
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Coloca a janela em modo fullscreen exclusivo
        device.setFullScreenWindow(this);

        // --- FIM DO CÓDIGO FULLSCREEN ---

        // this.setResizable(true); // Não é mais necessário em fullscreen

        // 2. Configuração do CardLayout
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // 3. Cria e adiciona o painel de login
        loginPanel = new LoginPanel(this);
        mainContainer.add(loginPanel, "LOGIN");

        // 4. Adiciona o container principal à janela
        this.add(mainContainer);
        // this.pack(); // Não é mais necessário, o fullscreen cuida do tamanho
        // this.setLocationRelativeTo(null); // Não é mais necessário
        this.setVisible(true); // Viser o último

        // 5. Adiciona o listener de fechamento
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (gameEngine != null) {
                    System.out.println("Janela fechando, salvando pontuação...");
                    gameEngine.stop();
                } else {
                    System.out.println("Janela fechada antes de iniciar o jogo.");
                }
                System.exit(0);
            }
        });
    } // --- Fim do Construtor GameWindow() ---
*/


    /**
     * Chamado pelo LoginPanel para iniciar o jogo.
     */
    public void startGame(int playerID) {
        // 1. Cria os componentes do jogo

        // >>>>>>> PARAR O MENU E TOCAR MÚSICA DA FASE <<<<<<<<
        audio.AudioManager.stopBackgroundMusic();
        audio.AudioManager.playBackgroundMusic("1-fase.wav");
        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


        gamePanel = new GamePanel();
        gameEngine = new GameEngine(gamePanel, playerID);

        // Linka o player criado no GameEngine ao GamePanel
        gamePanel.setPlayer(gameEngine.getPlayer());

        // 2. Adiciona o painel do jogo ao CardLayout
        mainContainer.add(gamePanel, "GAME");

        // 3. Troca a tela para o painel do jogo
        cardLayout.show(mainContainer, "GAME");

        // 4. Dá o foco ao GamePanel para que os inputs funcionem
        gamePanel.requestFocusInWindow();

        // 5. Inicia o loop do jogo
        gameEngine.start();
    }

} // --- Fim da Classe GameWindow ---