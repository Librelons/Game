package Main;

import Util.DatabaseConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.util.List;

public class LoginPanel extends JPanel {

    // Referências de componentes
    private GameWindow gameWindow;
    private BufferedImage CeuImg;
    private Font gameFont; // Nossa nova fonte 2D

    public LoginPanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;

        // 1. Carrega os recursos
        this.CeuImg = loadCeuImg();
        this.gameFont = loadCustomFont("VT323-Regular.ttf");

        audio.AudioManager.playBackgroundMusic("menu_loop.wav");

        // 2. Define o layout principal
        this.setLayout(new GridBagLayout());

        // 3. PAINEL DE LOGIN (CENTRAL)
        GridBagConstraints gbcCenter = new GridBagConstraints();
        JPanel centerPanel = createCentralPanel();
        gbcCenter.gridx = 0;
        gbcCenter.gridy = 0;
        gbcCenter.weightx = 1.0;
        gbcCenter.weighty = 1.0;
        gbcCenter.anchor = GridBagConstraints.CENTER;

        // --- MUDANÇA AQUI ---
        // Adiciona uma margem à DIREITA de 200px. Isso "empurra"
        // o painel de login 200px para a ESQUERDA do centro.
        // (top, left, bottom, right)
        gbcCenter.insets = new Insets(0, 0, 0, 100);

        this.add(centerPanel, gbcCenter);

        // 4. PAINEL DE RANKING (CANTO)
        GridBagConstraints gbcLeaderboard = new GridBagConstraints();
        JScrollPane leaderboardPanel = createLeaderboardPanel();
        gbcLeaderboard.gridx = 0;
        gbcLeaderboard.gridy = 0;
        gbcLeaderboard.weightx = 1.0;
        gbcLeaderboard.weighty = 1.0;
        gbcLeaderboard.anchor = GridBagConstraints.NORTHEAST;
        this.add(leaderboardPanel, gbcLeaderboard);
    }

    /**
     * Cria o painel centralizado com os campos de login.
     */
    private JPanel createCentralPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false); // Transparente

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        // Rótulo "Digite seu nome:"
        JLabel nameLabel = new JLabel("Digite seu nome:");
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setFont(gameFont.deriveFont(Font.PLAIN, 48f)); // Fonte 2D
        gbc.gridy = 0;
        panel.add(nameLabel, gbc);

        // Campo de texto
        JTextField nameField = new JTextField(20); // Largura 15
        nameField.setPreferredSize(new Dimension(250, 40));
        nameField.setFont(gameFont.deriveFont(Font.PLAIN, 28f)); // Fonte 2D
        gbc.gridy = 1;
        panel.add(nameField, gbc);

        // Botão "Iniciar Jogo"
        JButton startButton = new JButton("Iniciar Jogo");
        startButton.setPreferredSize(new Dimension(250, 40));
        startButton.setFont(gameFont.deriveFont(Font.PLAIN, 28f)); // Fonte 2D
        gbc.gridy = 2;
        panel.add(startButton, gbc);

        // Ação do botão
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playerName = nameField.getText();

                if (playerName == null || playerName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(LoginPanel.this,
                            "Por favor, digite um nome.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int playerID = DatabaseConnection.findOrCreatePlayer(playerName.trim());

                if (playerID != -1) {
                    gameWindow.startGame(playerID);
                } else {
                    JOptionPane.showMessageDialog(LoginPanel.this,
                            "Erro ao salvar jogador no banco de dados.", "Erro de DB", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    /**
     * Cria o painel da direita com o ranking.
     */
    private JScrollPane createLeaderboardPanel() {
        // Busca os scores
        List<String> scores = DatabaseConnection.getTopScores();

        // Cria a área de texto
        JTextArea leaderboardArea = new JTextArea();
        leaderboardArea.setEditable(false);
        leaderboardArea.setOpaque(false);
        leaderboardArea.setForeground(Color.BLACK);

        // --- MUDANÇA AQUI: Fonte do ranking maior ---
        leaderboardArea.setFont(gameFont.deriveFont(Font.PLAIN, 30f)); // AUMENTA A FONTE DO RANKING

        // Preenche o texto
        leaderboardArea.setText("RANKING - TOP 10\n\n");
        for (String score : scores) {
            leaderboardArea.append(score + "\n");
        }

        // Coloca dentro de um JScrollPane com margens
        JScrollPane scrollPane = new JScrollPane(leaderboardArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // --- MUDANÇA AQUI: Mais margem no TOPO ---
        // (top, left, bottom, right)
        scrollPane.setBorder(BorderFactory.createEmptyBorder(60, 20, 20, 80)); // Aumentei 'top' de 20 para 60

        return scrollPane;
    }

    /**
     * Carrega a fonte customizada da pasta /res
     */
    private Font loadCustomFont(String fontFileName) {
        try {
            InputStream is = getClass().getResourceAsStream("/res/" + fontFileName);
            if (is == null) {
                throw new IOException("Arquivo da fonte não encontrado: /res/" + fontFileName);
            }
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont);

            // Retorna a fonte base, com um tamanho padrão (que podemos mudar)
            return baseFont.deriveFont(12f);

        } catch (IOException | FontFormatException e) {
            System.err.println("Erro ao carregar a fonte customizada " + fontFileName);
            e.printStackTrace();
            // Retorna uma fonte padrão caso falhe
            return new Font("SansSerif", Font.PLAIN, 12);
        }
    }

    /**
     * Carrega a imagem de login da pasta /res
     */
    private BufferedImage loadCeuImg() {
        try {
            BufferedImage img = ImageIO.read(getClass().getResourceAsStream("/res/login_wallpaper.png"));
            if (img == null) {
                throw new IOException("Imagem não encontrada no caminho /res/login_wallpaper.png");
            }
            return img;

        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem de wallpaper:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Desenha o wallpaper.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (CeuImg != null) {
            g.drawImage(CeuImg, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // Define o tamanho preferido da janela
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(GamePanel.LARGURA_TELA, GamePanel.ALTURA_TELA);
    }
}